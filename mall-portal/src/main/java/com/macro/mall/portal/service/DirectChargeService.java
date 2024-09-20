package com.macro.mall.portal.service;

import com.alibaba.fastjson.JSONObject;

public interface DirectChargeService {

    void directCharge(String orderSN) ;

    void chargeSuccess(JSONObject callback) throws Exception;
}
