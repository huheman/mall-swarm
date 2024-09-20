package com.macro.mall.portal.service.impl;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.macro.mall.model.OmsOrderItem;
import com.macro.mall.portal.dao.DirectChargeDao;
import com.macro.mall.portal.domain.DirectChargeDomain;
import com.macro.mall.portal.service.DirectChargeService;
import com.macro.mall.portal.service.FeignAdminService;
import com.macro.mall.portal.service.OmsPortalOrderService;
import com.macro.mall.portal.service.WYTDChargeService;
import com.macro.mall.portal.service.bo.OmsOrderDeliveryParam;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class DirectChargeServiceImpl implements DirectChargeService {
    @Autowired
    private OmsPortalOrderService omsPortalOrderService;
    @Autowired
    private DirectChargeDao directChargeDao;
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

        OmsOrderItem omsOrderItem = omsPortalOrderService.selectOrderItemByOrderSN(orderSN);
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
        callback.remove("StatusMsg");
        String cards = (String) callback.remove("Cards");
        String signature = wytdChargeService.generateSignature(callback);
        Assert.equals(signature, sign, "回调签名不正确");
        Integer status = callback.getInteger("Status");
        String orderSN = callback.getString("UserOrderId");
        DirectChargeDomain chargeDomain = directChargeDao.selectByOrderSN(orderSN);
        try {
            Assert.state(status == 1, callback + "充值回调状态不为成功");

            if (StringUtils.hasLength(cards)) {
                String wytdOrderId = callback.getString("OrderId");
                // 如果涉及卡密，就记录起卡密
                cards = wytdChargeService.decryptCards(wytdOrderId, cards);
                /*形如：卡a,卡密a,有效期a|卡b,卡密b,有效期b|卡c,卡密c,有效期c
                有效期格式为 yyyy-MM-dd  ，2018-01-01
                123,456,2020-01-01|321,654,2020-01-01|666,777,2021-01-01
                */
                Assert.notEmpty(cards, "卡密解密失败");
                omsPortalOrderService.recordCards(orderSN, cards);
            }

            //发货
            OmsOrderDeliveryParam param = new OmsOrderDeliveryParam();
            param.setOrderId(chargeDomain.getOrderId());
            param.setDeliverySn(chargeDomain.getOrderSN());
            param.setDeliveryCompany("");
            feignAdminService.delivery(Arrays.asList(param));

            // 记录状态成功
            chargeDomain.success();
            directChargeDao.update(chargeDomain);
        } catch (Exception e) {
            chargeDomain.fail(e.getMessage());
            directChargeDao.update(chargeDomain);
        }

    }

    @Override
    public String chargeStatus(String orderSN) {

        return wytdChargeService.status(orderSN);
    }
}
