package com.macro.mall.portal.service;

import com.macro.mall.portal.service.bo.OpenIdBO;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;

public interface WxPayService {
    PrepayWithRequestPaymentResponse createOrder(String openId, Long orderId);

    OpenIdBO getOpenId(String code);

    void notifyPay(String requestBody, String signature, String serial, String nonc, String wechatTimestamp, String signType);

    String getPhone(String phoneCode);
}
