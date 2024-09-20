package com.macro.mall.portal.service;


import com.alibaba.fastjson.JSONObject;

/*伟业腾达直充相关接口*/
public interface WYTDChargeService {
    void createOrder(Long goodsId, Integer buyNum, String gameArea, String gameServer, String chargeAccount, String userOrderId) throws Exception;

    String generateSignature(JSONObject jsonObject);

    String status(String orderSN);
}
