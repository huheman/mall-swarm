package com.macro.mall.portal.service.impl;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.mapper.OmsOrderItemMapper;
import com.macro.mall.mapper.OmsOrderMapper;
import com.macro.mall.model.OmsOrderItem;
import com.macro.mall.model.OmsOrderItemExample;
import com.macro.mall.portal.dao.DirectChargeDao;
import com.macro.mall.portal.domain.DirectChargeDomain;
import com.macro.mall.portal.service.DirectChargeService;
import com.macro.mall.portal.service.FeignAdminService;
import com.macro.mall.portal.service.WYTDChargeService;
import com.macro.mall.portal.service.bo.OmsOrderDeliveryParam;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DirectChargeServiceImpl implements DirectChargeService {
    @Autowired
    private OmsOrderItemMapper omsOrderItemMapper;
    @Autowired
    private DirectChargeDao directChargeDao;
    @Autowired
    private OkHttpClient client;
    @Autowired
    private OmsOrderMapper orderMapper;
    @Autowired
    private FeignAdminService feignAdminService;
    @Autowired
    private WYTDChargeService wytdChargeService;

    private static final String PRE_FIX = "charge-";

    /*把订单直充*/
    @Override
    @Async
    public void directCharge(String orderSN) {
        Assert.notEmpty(orderSN, "orderSN is null or empty");

        OmsOrderItemExample example = new OmsOrderItemExample();
        example.createCriteria().andOrderSnEqualTo(orderSN);
        List<OmsOrderItem> omsOrderItems = omsOrderItemMapper.selectByExample(example);
        Assert.notEmpty(omsOrderItems, "直充时找到的项目为空");
        OmsOrderItem omsOrderItem = omsOrderItems.get(0);
        String productSkuCode = omsOrderItem.getProductSkuCode();
        if (StringUtils.startsWithIgnoreCase(productSkuCode, PRE_FIX)) {
            doDirectCharge(productSkuCode.substring(PRE_FIX.length()), omsOrderItem);
        } else {
            log.info("商品的skuCode是{}无需直充", productSkuCode);
        }
    }

    private void doDirectCharge(String chargeId, OmsOrderItem omsOrderItem) {
        Assert.notEmpty(chargeId, "chargeId is null or empty");
        DirectChargeDomain chargeDomain = directChargeDao.selectByOrderSN(omsOrderItem.getOrderSn());
        // 如果充值记录已经存在了，并且充值状态不是充值失败，那么就不要处理了
        if (chargeDomain == null) {
            chargeDomain = new DirectChargeDomain(omsOrderItem.getOrderSn(), omsOrderItem.getOrderId());
            directChargeDao.insert(chargeDomain);
        } else if (chargeDomain.getChargeStatus() != 3) {
            log.info("充值记录已经存在，且充值状态不是充值失败，不处理了{}", chargeDomain);
            return;
        }
        String productAttr = omsOrderItem.getProductAttr();
        // 如果有多个orderItem只取第一个
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
        try {
            wytdChargeService.createOrder(Long.valueOf(chargeId), omsOrderItem.getProductQuantity(),
                    productAttrMap.get("areaServer"),
                    productAttrMap.get("server"),
                    productAttrMap.get("username"), omsOrderItem.getOrderSn());
        } catch (Exception e) {
            log.error("直充失败了", e);
            chargeDomain.fail(e.getMessage());
            directChargeDao.update(chargeDomain);
        }

    }

    @Override
    @SneakyThrows
    public void chargeSuccess(JSONObject callback) {
        log.info("直充回调接口被触发了{}", callback);
        String sign = (String) callback.remove("Sign");
        String signature = wytdChargeService.generateSignature(callback);
        Assert.equals(signature, sign, "回调签名不正确");
        Integer status = callback.getInteger("Status");
        String orderSN = callback.getString("UserOrderId");
        DirectChargeDomain chargeDomain = directChargeDao.selectByOrderSN(orderSN);
        try {
            Assert.state(status == 1, callback + "充值回调状态不为成功");
            OmsOrderDeliveryParam param = new OmsOrderDeliveryParam();
            param.setOrderId(chargeDomain.getOrderId());
            param.setDeliverySn(chargeDomain.getOrderSN());
            param.setDeliveryCompany("");
            CommonResult delivery = feignAdminService.delivery(Arrays.asList(param));
            log.info("直充完成后调用发货结果{}", delivery);
            chargeDomain.success();
            directChargeDao.update(chargeDomain);
        } catch (Exception e) {
            chargeDomain.fail(e.getMessage());
            directChargeDao.update(chargeDomain);
        }

    }
}
