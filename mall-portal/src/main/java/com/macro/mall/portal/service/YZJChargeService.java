package com.macro.mall.portal.service;


import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/*伟业腾达直充相关接口*/
public interface YZJChargeService {
    void createOrder(Long goodsId, Integer buyNum, String gameArea, String gameServer, String chargeAccount, String userOrderId) throws Exception;

    Map<String, Object> decryptData(String encryptedData);
}
