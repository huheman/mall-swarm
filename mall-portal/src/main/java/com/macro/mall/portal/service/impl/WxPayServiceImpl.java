package com.macro.mall.portal.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.macro.mall.common.service.RedisService;
import com.macro.mall.portal.domain.OmsOrderDetail;
import com.macro.mall.portal.service.WxPayService;
import com.macro.mall.portal.service.bo.OpenIdBO;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class WxPayServiceImpl implements WxPayService {
    /**
     * 商户号
     */
    @Value("${wx.merchantId}")
    public String merchantId;
    /**
     * 商户API私钥路径
     */
    @Value("${wx.privateKey}")
    public String privateKey;
    /**
     * 商户证书序列号
     */
    @Value("${wx.merchantSerialNumber}")
    public String merchantSerialNumber;
    /**
     * 商户APIV3密钥
     */
    @Value("${wx.apiV3Key}")
    public String apiV3Key;
    @Value("${wx.appId}")
    public String appId;
    @Value("${wx.appSecret}")
    private String appSecret;
    @Value("${wx.notifyUrl}")
    private String notifyUrl;
    @Autowired
    private JsapiServiceExtension jsapiServiceExtension;
    @Autowired
    private OkHttpClient okHttpClient;
    @Autowired
    private NotificationConfig notificationConfig;
    @Autowired
    private OmsPortalOrderServiceImpl orderService;
    @Autowired
    private OmsPortalOrderServiceImpl portalOrderService;
    @Autowired
    private DirectChargeServiceImpl directChargeService;
    @Autowired
    private RedisService redisService;

    @Override
    public PrepayWithRequestPaymentResponse createOrder(String openId, Long orderId) {
        // request.setXxx(val)设置所需参数，具体参数可见Request定义
        // 跟之前下单示例一样，填充预下单参数
        OmsOrderDetail detail = orderService.detail(orderId);
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        int payAmount = detail.getPayAmount().multiply(new BigDecimal("100")).intValue();
        amount.setTotal(payAmount);
        request.setAmount(amount);
        request.setAppid(appId);
        request.setMchid(merchantId);
        request.setDescription(detail.getOrderSn());
        request.setNotifyUrl(notifyUrl);
        Payer payer = new Payer();
        payer.setOpenid(openId);
        request.setPayer(payer);
        request.setOutTradeNo(detail.getOrderSn());
        return jsapiServiceExtension.prepayWithRequestPayment(request);
    }

    @Override
    public OpenIdBO getOpenId(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, code
        );

        // 构建 OkHttp 请求
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept-Encoding", "identity")
                .build();

        // 发送请求并获取响应
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalArgumentException("ok");
            }
            // 解析响应体
            String responseBody = response.body().string();
            log.info("获取openId成功", responseBody);
            return JSON.parseObject(responseBody, OpenIdBO.class);
        } catch (Exception e) {
            log.error("获取openId失败", e);
            return null;
        }
    }

    public void uploadShipping(Long orderId) {
        String accessToken = getAccessToken();
        OmsOrderDetail detail = orderService.detail(orderId);
        if (detail.getPayType() != 2) {
            // 如果不是微信支付，不用微信发货
            return;
        }
        String url = String.format("https://api.weixin.qq.com/wxa/sec/order/upload_shipping_info?access_token=%s", accessToken);
        // 获取当前时间
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String formattedDate = now.format(formatter);
        JSONObject moreInfo = new JSONObject(detail.getMoreInfo());
        String openId = moreInfo.getStr("openId");
        String body = String.format("{\n" +
                "    \"order_key\": {\n" +
                "        \"order_number_type\": 1,\n" +
                "        \"mchid\": \"%s\",\n" +
                "        \"out_trade_no\": \"%s\"\n" +
                "    },\n" +
                "    \"delivery_mode\": 1,\n" +
                "    \"logistics_type\": 3,\n" +
                "    \"shipping_list\": [\n" +
                "        {\n" +
                "            \"tracking_no\": \"\",\n" +
                "            \"express_company\": \"\",\n" +
                "            \"item_desc\": \"%s\",\n" +
                "            \"contact\": null\n" +
                "        }\n" +
                "    ],\n" +
                "    \"upload_time\": \"%s\",\n" +
                "    \"payer\": {\n" +
                "        \"openid\": \"%s\"\n" +
                "    }\n" +
                "}", merchantId, detail.getOrderSn(), detail.getOrderSn(), formattedDate, openId);
        log.info("ship body :{}", body);
        RequestBody requestBody = RequestBody.create(body, MediaType.parse("application/json; charset=utf-8"));

        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            // 获取响应体内容
            JSONObject entries = new JSONObject(response.body().string());
            log.info("entries from ship:{}", entries);
            Integer errcode = entries.getInt("errcode");
            Assert.state(errcode == 0, "微信发货失败" + entries.getStr("errmsg"));
        } catch (IOException e) {
            log.error("发货失败", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notifyPay(String requestBody, String signature, String serial, String nonc, String wechatTimestamp, String signType) {
        com.wechat.pay.java.core.notification.RequestParam requestParam = new com.wechat.pay.java.core.notification.RequestParam.Builder()
                .serialNumber(serial)
                .nonce(nonc)
                .signature(signature)
                .signType(signType)
                .timestamp(wechatTimestamp)
                .body(requestBody)
                .build();

        NotificationParser parser = new NotificationParser(notificationConfig);
        try {
            // 以支付通知回调为例，验签、解密并转换成 Transaction
            Transaction transaction = parser.parse(requestParam, Transaction.class);
            log.info("微信回调生效" + JSON.toJSONString(transaction));
            if (transaction.getTradeState() == Transaction.TradeStateEnum.SUCCESS) {
                String outTradeNo = transaction.getOutTradeNo();
                orderService.updateNote(outTradeNo, transaction.getPayer().getOpenid());
                portalOrderService.paySuccessByOrderSn(outTradeNo, 2);
                directChargeService.directCharge(outTradeNo);
            } else {
                log.error("收到付款失败信息:{}", transaction);
            }
        } catch (ValidationException e) {
            log.error("解密微信回调失败", e);
            throw e;
        }
    }

    /*https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-info/phone-number/getPhoneNumber.html*/
    @Override
    public String getPhone(String code) {
        String accessToken = getAccessToken();
        String url = String.format("https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=%s", accessToken);
        String jsonBody = "{\"code\": \"" + code + "\"}";
        RequestBody requestBody = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));

        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            // 获取响应体内容
            JSONObject entries = new JSONObject(response.body().string());
            return entries.getJSONObject("phone_info").getStr("purePhoneNumber");
        } catch (IOException e) {
            log.error("获取手机号失败", e);
            return "";
        }
    }

    @SneakyThrows
    private String getAccessToken() {
        String accessToken = (String) redisService.get("accessToken");
        if (StringUtils.isEmpty(accessToken)) {
            String url = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, appSecret);
            Request request = new Request.Builder().url(url).build();
            try (Response response = okHttpClient.newCall(request).execute()) {
                JSONObject jsonObject = new JSONObject(response.body().string());
                accessToken = jsonObject.getStr("access_token");
                // 一个小时有效
                redisService.set("accessToken", accessToken, 60 * 60);
                return accessToken;
            } catch (Exception e) {
                log.error("获取accesstoken失败", e);
                throw e;
            }
        } else {
            return accessToken;
        }
    }
}
