package com.macro.mall.portal.service;


import java.util.Map;

/*伟业腾达直充相关接口*/
public interface YZJChargeService {
    void createOrder(Long goodsId, Integer buyNum, String gameArea, String gameServer, String chargeAccount,Integer buyCount, String userOrderId) throws Exception;

    Map<String, Object> decryptData(String encryptedData);
}
