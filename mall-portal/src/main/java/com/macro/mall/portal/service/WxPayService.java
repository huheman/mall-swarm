package com.macro.mall.portal.service;

import com.macro.mall.model.OmsOrder;
import com.macro.mall.portal.service.bo.OpenIdBO;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.model.RefundNotification;

public interface WxPayService {
    PrepayWithRequestPaymentResponse createOrder(String openId, OmsOrder omsOrder);

    OpenIdBO getOpenId(String code);

    Transaction notifyPay(String requestBody, String signature, String serial, String nonc, String wechatTimestamp, String signType);

    String getPhone(String phoneCode);

    void uploadShipping(OmsOrder orderId);

    void refund(OmsOrder omsOrder);

    RefundNotification notifyRefund(String requestBody, String signature, String serial, String nonc, String wechatTimestamp, String signType);

    byte[] qrCodePic(String scene);
}
