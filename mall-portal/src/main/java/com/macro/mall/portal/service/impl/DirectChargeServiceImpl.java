package com.macro.mall.portal.service.impl;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.mapper.OmsOrderItemMapper;
import com.macro.mall.mapper.OmsOrderMapper;
import com.macro.mall.model.OmsOrder;
import com.macro.mall.model.OmsOrderExample;
import com.macro.mall.model.OmsOrderItem;
import com.macro.mall.model.OmsOrderItemExample;
import com.macro.mall.portal.dao.DirectChargeDao;
import com.macro.mall.portal.domain.DirectChargeDomain;
import com.macro.mall.portal.service.DirectChargeService;
import com.macro.mall.portal.service.FeignAdminService;
import com.macro.mall.portal.service.bo.OmsOrderDeliveryParam;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Component
@Slf4j
public class DirectChargeServiceImpl implements DirectChargeService {
    @Autowired
    private OmsOrderItemMapper omsOrderItemMapper;
    @Autowired
    private DirectChargeDao directChargeDao;
    @Value("${xhs.appId}")
    private String appId;
    @Value("${xhs.url}")
    private String url;
    @Value("${xhs.appSecret}")
    private String appSecret;
    @Value("${xhs.notifyUrl}")
    private String notifyUrl;
    @Autowired
    private OkHttpClient client;
    @Autowired
    private OmsOrderMapper orderMapper;
    @Autowired
    private FeignAdminService feignAdminService;
    @Autowired
    private OmsPortalOrderServiceImpl portalOrderService;


    private static final byte[] DIGITS = new byte[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static final String SIGNATURE_RAW_TEMPLATE = "%s@%s";

    @SneakyThrows
    public static String generateSignature(String appSecret, String timestampStr, TreeMap<String, String> sortedParams) throws NoSuchAlgorithmException {
        StringBuilder queryStringBuilder = new StringBuilder();
        Set<Map.Entry<String, String>> entries = sortedParams.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            queryStringBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        String signatureRaw = String.format(SIGNATURE_RAW_TEMPLATE, queryStringBuilder.substring(0, queryStringBuilder.length() - 1), timestampStr);

        SecretKeySpec secretKeySpec = new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);
        byte[] signatureBytes = mac.doFinal(signatureRaw.getBytes(StandardCharsets.UTF_8));

        return encode(signatureBytes);
    }

    private static String encode(byte[] data) {
        int len = data.length;
        byte[] out = new byte[len << 1];
        for (int i = 0, j = 0; i < len; i++) {
            out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS[0xF & data[i]];
        }
        return new String(out, StandardCharsets.UTF_8);
    }

    /*把订单直充*/
    @Override
    @Async
    public void directCharge(String orderSN) {
        if (orderSN == null || orderSN.isEmpty()) {
            log.error("orderSN is null or empty");
            return;
        }

        DirectChargeDomain chargeDomain = directChargeDao.selectByOrderSN(orderSN);
        // 如果充值记录已经存在了，并且充值状态不是充值失败，那么就不要处理了
        if (chargeDomain == null) {
            OmsOrderExample omsOrderExample = new OmsOrderExample();
            omsOrderExample.createCriteria().andOrderSnEqualTo(orderSN);
            List<OmsOrder> omsOrders = orderMapper.selectByExample(omsOrderExample);
            if (CollectionUtils.isEmpty(omsOrders)) {
                log.error(orderSN + "orderSN对应的订单为空");
                return;
            }
            Long orderId = omsOrders.get(0).getId();
            chargeDomain = new DirectChargeDomain(orderSN, orderId);
            directChargeDao.insert(chargeDomain);
        } else if (chargeDomain.getChargeStatus() != 3) {
            log.info("充值记录已经存在，且充值状态不是充值失败，不处理了{}", chargeDomain);
            return;
        }

        OmsOrderItemExample example = new OmsOrderItemExample();
        example.createCriteria().andOrderSnEqualTo(orderSN);
        List<OmsOrderItem> omsOrderItems = omsOrderItemMapper.selectByExample(example);
        Assert.notEmpty(omsOrderItems, "直充时找到的项目为空");
        OmsOrderItem omsOrderItem = omsOrderItems.get(0);
        String productAttr = omsOrderItem.getProductAttr();
        String productSkuCode = omsOrderItem.getProductSkuCode();
        if (productSkuCode == null || !productSkuCode.startsWith("xhs-")) {
            log.info("小海兽的充值接口，skuCode必须以xhs-开头，并接上小海兽的id");
            return;
        }
        String[] split = productSkuCode.split("-");
        if (split.length != 2) {
            log.info("小海兽的充值接口，skuCode必须以xhs-开头，并接上小海兽的id");
            return;
        }
        String commodityId = split[1];
        // 如果有多个orderItem只取第一个
        try {
            Map<String, String> productAttrMap = new HashMap<>();
            JSONArray spData = JSON.parseArray(productAttr);
            for (int i = 0; i < spData.size(); i++) {
                JSONObject unit = spData.getJSONObject(i);
                String value = unit.getString("value");
                if (StringUtils.hasLength(value)) {
                    int idx = value.indexOf('-');
                    if (idx > 0 && value.length() - 1 > idx) {
                        value = value.substring(idx + 1);
                    }
                    String key = unit.getString("key");
                    int keyIdx = key.indexOf('-');
                    if (keyIdx > 0 && key.length() - 1 > keyIdx) {
                        productAttrMap.put(key.substring(0, keyIdx), value);
                        productAttrMap.put(key.substring(keyIdx + 1), value);
                    } else {
                        productAttrMap.put(key, value);
                    }
                }
            }

            TreeMap<String, String> sortedParams = new TreeMap<>();
            MediaType mediaType = MediaType.parse("application/json");
            sortedParams.put("commodityId", commodityId);
            sortedParams.put("nickname", productAttrMap.get("nickname"));
            sortedParams.put("username", productAttrMap.get("username"));
            sortedParams.put("password", productAttrMap.get("password"));
            sortedParams.put("server", productAttrMap.get("server"));
            sortedParams.put("areaServer", productAttrMap.get("areaServer"));
            /*暂定平台库存，客户库存还没做*/
            sortedParams.put("inventoryType", "1");
            sortedParams.put("outOrderId", orderSN);
            sortedParams.put("notifyUrl", notifyUrl);
            RequestBody body = RequestBody.create(mediaType, JSON.toJSONString(sortedParams));
            sortedParams.clear();
            sortedParams.put("appId", this.appId);
            String timeStr = System.currentTimeMillis() / 1000 + "";
            String signature = generateSignature(this.appSecret, timeStr, sortedParams);
            Request request = new Request.Builder()
                    .url(url + "/api/open/orders?appId=" + this.appId)
                    .method("POST", body)
                    .addHeader("Accept", "application/json")
                    .addHeader("X-Signature", signature)
                    .addHeader("X-Timestamp", timeStr)
                    .addHeader("Authorization", "")
                    .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            String string = response.body().string();
            log.info("小海兽返回值:" + string);
            Integer code = JSON.parseObject(string).getInteger("code");
            Assert.state(code != null && code.equals(200), "小海兽返回值不是200");
        } catch (Exception e) {
            log.error(orderSN + "调用直充接口发生异常", e);
            chargeDomain.fail();
            directChargeDao.update(chargeDomain);
        }
    }

    @Override
    @SneakyThrows
    public void chargeSuccess(String singature, String timestampStr, JSONObject body) {
        log.info("直充回调接口被触发了{},{},{}", singature, timestampStr, body);
        TreeMap<String, String> sortedMap = new TreeMap<>();
        sortedMap.put("appId", this.appId);
        String s = generateSignature(appSecret, timestampStr, sortedMap);
        if (!s.equals(singature)) {
            log.error("直充通知回调接口的签名不符合规则" + body);
            throw new IllegalAccessException("直充通知回调接口的签名不符合规则");
        }
        long secondGap = (System.currentTimeMillis() - Long.parseLong(timestampStr) * 1000) / 1000;
        if (secondGap > 120) {
            log.error("直充通知回调接口的签名时间距离现在超过120秒了，无效");
            throw new IllegalAccessException("直充通知回调接口的签名时间距离现在超过120秒了，无效");

        }
        String orderSN = body.getString("outOrderId");
        String tradeState = body.getString("tradeState");
        if (orderSN == null || orderSN.isEmpty()) {
            log.error("orderSN is null or empty");
            throw new IllegalAccessException("orderSN is null or empty");

        }
        if (tradeState == null || tradeState.isEmpty()) {
            log.error("tradeState is null or empty");
            throw new IllegalAccessException("tradeState is null or empty");

        }

        DirectChargeDomain chargeDomain = directChargeDao.selectByOrderSN(orderSN);
        if (tradeState.equals("SUCCESS")) {
            log.info("直充接口直充成功");

            OmsOrderDeliveryParam param = new OmsOrderDeliveryParam();
            param.setOrderId(chargeDomain.getOrderId());
            param.setDeliverySn(chargeDomain.getOrderSN());
            param.setDeliveryCompany("小海兽");
            CommonResult delivery = feignAdminService.delivery(Arrays.asList(param));
            if (delivery.getCode() != 200) {
                throw new IllegalAccessException("发货失败" + delivery.getMessage());
            }
            chargeDomain.success();
            directChargeDao.update(chargeDomain);
            // 不用用户直接确认收货
            // portalOrderService.confirmReceiveOrder(chargeDomain.getOrderId());
        } else {
            log.error("直充接口失败，失败原因是" + body.getString("failReason"));
        }
    }
}
