package com.macro.mall.portal.service.impl;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.macro.mall.common.exception.ApiException;
import com.macro.mall.mapper.DirectChargeMapper;
import com.macro.mall.model.DirectCharge;
import com.macro.mall.model.DirectChargeExample;
import com.macro.mall.model.OmsOrderItem;
import com.macro.mall.portal.component.SmsSender;
import com.macro.mall.portal.domain.OmsOrderDetail;
import com.macro.mall.portal.service.*;
import com.macro.mall.portal.service.bo.OmsOrderDeliveryParam;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class DirectChargeServiceImpl implements DirectChargeService {
    @Autowired
    private OmsPortalOrderService omsPortalOrderService;
    @Autowired
    private DirectChargeMapper directChargeMapper;
    @Autowired
    private FeignAdminService feignAdminService;
    @Autowired
    private WYTDChargeService wytdChargeService;
    @Autowired
    private YZJChargeService yzjChargeService;

    @Autowired
    private SmsSender smsSender;
    @Value("${app.admin.phones}")
    private String adminPhones;
    @Value("${sms.manualChargeId}")
    private String smsManualChargeId;
    @Value("${sms.directChargeFailId}")
    private String directChargeFailId;

    private static final String PRE_FIX_WYTD = "charge";
    private static final String PRE_FIX_YZJ = "yzj";

    /*把订单直充*/
    @Override
    @Async
    public void directCharge(String orderSN) {
        Assert.notEmpty(orderSN, "orderSN is null or empty");
        OmsOrderItem omsOrderItem = omsPortalOrderService.selectOrderItemByOrderSN(orderSN);
        OmsOrderDetail orderDetail = omsPortalOrderService.detail(omsOrderItem.getOrderId());
        if (orderDetail.getStatus() != 1) {
            throw new ApiException("订单" + orderSN + "状态不是已付款，不能直充");
        }
        String productSkuCode = omsOrderItem.getProductSkuCode();
        if (org.apache.commons.lang3.StringUtils.startsWithAny(productSkuCode, PRE_FIX_WYTD, PRE_FIX_YZJ)) {
            doDirectCharge(productSkuCode.split("-")[0], productSkuCode.split("-")[1], omsOrderItem);
        } else {
            log.info("商品的skuCode是{}无需直充", productSkuCode);
            // 发短信通知要代充
            smsSender.send(Arrays.stream(adminPhones.split(",")).toList(), Collections.EMPTY_LIST, smsManualChargeId);
        }
    }

    private DirectCharge selectByOrderSN(String orderSN) {
        DirectChargeExample directChargeExample = new DirectChargeExample();
        directChargeExample.createCriteria().andOrderSnEqualTo(orderSN);
        List<DirectCharge> directCharges = directChargeMapper.selectByExample(directChargeExample);
        if (directCharges.size() > 0) {
            return directCharges.get(0);
        }
        return null;
    }

    private void doDirectCharge(String prefix, String chargeId, OmsOrderItem omsOrderItem) {
        Assert.notEmpty(chargeId, "chargeId is null or empty");
        DirectCharge directCharge = selectByOrderSN(omsOrderItem.getOrderSn());
        // 如果充值记录已经存在了，并且充值状态不是充值失败，那么就不要处理了
        if (directCharge == null) {
            directCharge = new DirectCharge();
            directCharge.setOrderSn(omsOrderItem.getOrderSn());
            directCharge.setOrderId(omsOrderItem.getOrderId());
            directCharge.setChargeStatus(1);
            directCharge.setCreateTime(new Date());
            directChargeMapper.insert(directCharge);
        } else if (directCharge.getChargeStatus() != 3) {
            log.info("充值记录已经存在，且充值状态不是充值失败，不处理了{}", directCharge);
            return;
        }
        String productAttr = omsOrderItem.getProductAttr();
        // 如果有多个orderItem只取第一个
        Map<String, String> productAttrMap = new HashMap<>();
        JSONArray spData = JSON.parseArray(productAttr);
        for (int i = 0; i < spData.size(); i++) {
            JSONObject unit = spData.getJSONObject(i);
            String value = unit.getString("value");
            if (StringUtils.isNoneEmpty(value)) {
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
        try {
            if (prefix.equals(PRE_FIX_WYTD)) {
                JSONObject moreInfo = new JSONObject();
                for (Integer i = 0; i < omsOrderItem.getProductQuantity(); i++) {
                    String subOrderSN = omsOrderItem.getOrderSn() + "-" + i;
                    moreInfo.put(subOrderSN, "waiting");
                }
                directCharge.setMoreInfo(moreInfo.toJSONString());
                directChargeMapper.updateByPrimaryKeySelective(directCharge);

                for (String subOrderSN : moreInfo.keySet()) {
                    wytdChargeService.createOrder(Long.valueOf(chargeId), 1,
                            productAttrMap.get("areaServer"),
                            productAttrMap.get("server"),
                            productAttrMap.get("username"), subOrderSN);
                }

            } else if (prefix.equals(PRE_FIX_YZJ)) {
                String buyCount = productAttrMap.get("buyCount");
                yzjChargeService.createOrder(Long.valueOf(chargeId), omsOrderItem.getProductQuantity(),
                        productAttrMap.get("areaServer"),
                        productAttrMap.get("server"),
                        productAttrMap.get("username"), extractNumber(buyCount), omsOrderItem.getOrderSn());
            }
        } catch (Exception e) {
            log.error("直充失败了", e);
            directCharge.setChargeStatus(3);
            directCharge.setFailReason(StringUtils.substring(e.getMessage(), 0, 300));
            directChargeMapper.updateByPrimaryKey(directCharge);
        }

    }

    private Integer extractNumber(String input) {
        if (StringUtils.isEmpty(input)) {
            return null;
        }
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return null;
    }

    @Override
    @SneakyThrows
    public void chargeSuccess(JSONObject callback) {
        log.info("直充回调接口被触发了{}", callback);
        String sign = (String) callback.remove("Sign");
        callback.remove("StatusMsg");
        String cards = (String) callback.remove("Cards");
        String signature = wytdChargeService.generateSignature(callback);
        Assert.equals(signature, sign, "回调签名不正确");
        Integer status = callback.getInteger("Status");
        String oriOrderSN = callback.getString("UserOrderId");
        String orderSN = oriOrderSN.substring(0, oriOrderSN.indexOf("-"));
        DirectCharge directCharge = selectByOrderSN(orderSN);
        try {
            String wytdOrderId = callback.getString("OrderId");
            cards = wytdChargeService.decryptCards(wytdOrderId, cards);
            if (StringUtils.isNoneEmpty(cards)) {
                omsPortalOrderService.recordCards(directCharge.getOrderSn(), cards);
            }
            Assert.state(status == 1, callback + "充值回调状态不为成功");
            onWytdSuccess(directCharge, oriOrderSN);
        } catch (Exception e) {
            onWytdFail(directCharge, oriOrderSN, e.getMessage());
            log.error("直充回调失败了", e);
            onDirectChargeFail(e.getMessage(), directCharge);
            smsSender.send(Arrays.stream(adminPhones.split(",")).toList(), Collections.EMPTY_LIST, directChargeFailId);
        }
    }

    private void onWytdFail(DirectCharge directCharge, String oriOrderSN, String message) {
        String moreInfo = directCharge.getMoreInfo();
        JSONObject moreInfoJson = JSON.parseObject(moreInfo);
        moreInfoJson.put(oriOrderSN, "fail:" + StringUtils.substring(message, 0, 30));
        directCharge.setMoreInfo(moreInfoJson.toJSONString());
        directChargeMapper.updateByPrimaryKey(directCharge);
        String failReason = StringUtils.trimToEmpty(directCharge.getFailReason()) + "订单" + oriOrderSN + "充值失败。";
        onDirectChargeFail(failReason, directCharge);
    }

    private void onWytdSuccess(DirectCharge directCharge, String oriOrderSN) {
        String moreInfo = directCharge.getMoreInfo();
        JSONObject moreInfoJson = JSON.parseObject(moreInfo);
        moreInfoJson.put(oriOrderSN, "ok");
        directCharge.setMoreInfo(moreInfoJson.toJSONString());
        directChargeMapper.updateByPrimaryKey(directCharge);
        boolean allGood = true;
        for (String subOrderSN : moreInfoJson.keySet()) {
            String subStatus = moreInfoJson.getString(subOrderSN);
            if (!StringUtils.equals(subStatus, "ok")) {
                allGood = false;
                break;
            }
        }
        if (allGood) {
            onChargeSuccess(directCharge);
        }
    }

    private void onDirectChargeFail(String failReason, DirectCharge directCharge) {
        directCharge.setChargeStatus(3);
        directCharge.setFailReason(StringUtils.substring(failReason, 0, 300));
        directChargeMapper.updateByPrimaryKey(directCharge);
        smsSender.send(Arrays.stream(adminPhones.split(",")).toList(), Collections.EMPTY_LIST, directChargeFailId);
    }

    public void yzjChargeSuccess(String data) {
        log.info("叶之家直充回调接口被触发了{}", data);
        JSONObject jsonObject = new JSONObject(yzjChargeService.decryptData(data));
        log.info("叶之家解密结果{}", jsonObject);
        String orderSN = jsonObject.getString("customer_order_id");
        Assert.notEmpty(orderSN, "orderSN is null or empty");
        DirectCharge directCharge = selectByOrderSN(orderSN);

        try {
            Assert.state("成功".equals(jsonObject.getString("order_state")), "充值回调状态不为成功");
            JSONArray cardList = Optional.ofNullable(jsonObject.getJSONArray("cards")).orElse(new JSONArray());

            List<String> sb = new ArrayList<>();
            for (int i = 0; i < cardList.size(); i++) {
                JSONObject tmp = cardList.getJSONObject(i);
                sb.add(tmp.getString("card_no") + "," + tmp.getString("card_pwd") + "," + tmp.getString("card_deadline"));
            }
            String cards = String.join("|", sb);
            if (StringUtils.isNoneEmpty(cards)) {
                omsPortalOrderService.recordCards(directCharge.getOrderSn(), cards);
            }
            onChargeSuccess(directCharge);
        } catch (Exception e) {
            log.error("咔之家直充失败了");
            onDirectChargeFail(e.getMessage(), directCharge);
        }
    }


    /*形如：卡a,卡密a,有效期a|卡b,卡密b,有效期b|卡c,卡密c,有效期c
                有效期格式为 yyyy-MM-dd  ，2018-01-01
                123,456,2020-01-01|321,654,2020-01-01|666,777,2021-01-01
                */
    private void onChargeSuccess(DirectCharge directCharge) {


        //发货
        OmsOrderDeliveryParam param = new OmsOrderDeliveryParam();
        param.setOrderId(directCharge.getOrderId());
        param.setDeliverySn(directCharge.getOrderSn());
        param.setDeliveryCompany("");
        feignAdminService.delivery(Arrays.asList(param));

        // 记录状态成功
        directCharge.setChargeStatus(2);
        directCharge.setFailReason("");
        directChargeMapper.updateByPrimaryKeySelective(directCharge);
    }

    @Override
    public String chargeStatus(String orderSN) {

        return wytdChargeService.status(orderSN);
    }
}
