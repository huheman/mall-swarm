package com.macro.mall.portal.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.transaction.annotation.Transactional;

public interface DirectChargeService {

    void directCharge(String orderSN);

    @Transactional
    void chargeSuccess(JSONObject callback) throws Exception;

    String chargeStatus(String orderSN);

    void yzjChargeSuccess(String data);
}
