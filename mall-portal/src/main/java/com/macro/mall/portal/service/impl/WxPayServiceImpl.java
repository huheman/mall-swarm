package com.macro.mall.portal.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.macro.mall.common.exception.ApiException;
import com.macro.mall.common.service.RedisService;
import com.macro.mall.model.OmsOrder;
import com.macro.mall.portal.service.WxPayService;
import com.macro.mall.portal.service.bo.OpenIdBO;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.*;
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
    @Value("${wx.refund.notifyUrl}")
    private String refundNotifyUrl;
    /**
     * 商户证书序列号
     */
    @Value("${wx.merchantSerialNumber}")
    public String merchantSerialNumber;

    @Value("${wx.env.version}")
    private String version;
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
    private RedisService redisService;
    @Autowired
    private RefundService refundService;

    @Override
    public PrepayWithRequestPaymentResponse createOrder(String openId, OmsOrder omsOrder) {
        // request.setXxx(val)设置所需参数，具体参数可见Request定义
        // 跟之前下单示例一样，填充预下单参数
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        int payAmount = omsOrder.getPayAmount().multiply(new BigDecimal("100")).intValue();
        amount.setTotal(payAmount);
        request.setAmount(amount);
        request.setAppid(appId);
        request.setMchid(merchantId);
        request.setDescription(omsOrder.getOrderSn());
        request.setNotifyUrl(notifyUrl);
        Payer payer = new Payer();
        payer.setOpenid(openId);
        request.setPayer(payer);
        request.setOutTradeNo(omsOrder.getOrderSn());
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
                throw new IOException("Unexpected code " + response);
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

    public void uploadShipping(OmsOrder omsOrder) {
        String accessToken = getAccessToken();
        if (omsOrder.getPayType() != 2) {
            // 如果不是微信支付，不用微信发货
            return;
        }
        String url = String.format("https://api.weixin.qq.com/wxa/sec/order/upload_shipping_info?access_token=%s", accessToken);
        // 获取当前时间
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String formattedDate = now.format(formatter);
        JSONObject moreInfo = new JSONObject(omsOrder.getMoreInfo());
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
                "}", merchantId, omsOrder.getOrderSn(), omsOrder.getOrderSn(), formattedDate, openId);
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
            Assert.state(errcode == 0 || errcode.equals(10060023), "微信发货失败" + entries.getStr("errmsg"));
        } catch (IOException e) {
            log.error("发货失败", e);
            throw new RuntimeException(e);
        }
    }

    /*https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_1_9.shtml*/
    @Override
    public void refund(OmsOrder omsOrder, Double amount) {
        CreateRequest createRequest = new CreateRequest();
        createRequest.setOutTradeNo(omsOrder.getOrderSn());
        createRequest.setOutRefundNo(omsOrder.getOrderSn());
        createRequest.setNotifyUrl(refundNotifyUrl);
        long payAmount = omsOrder.getPayAmount().multiply(new BigDecimal("100")).longValue();

        BigDecimal refundAmount = omsOrder.getPayAmount();
        if (amount != null) {
            Assert.state(new BigDecimal(amount).compareTo(refundAmount) <= 0, "退款金额不能大于订单金额");
            refundAmount = new BigDecimal(amount);
        }
        long refundAmountL = refundAmount.multiply(new BigDecimal("100")).longValue();
        AmountReq amountReq = new AmountReq();
        amountReq.setTotal(payAmount);
        amountReq.setCurrency("CNY");
        amountReq.setRefund(refundAmountL);
        createRequest.setAmount(amountReq);
        Refund refund = refundService.create(createRequest);
        Assert.state(refund.getStatus() == Status.SUCCESS || refund.getStatus() == Status.PROCESSING, "当前订单状态为" + refund.getStatus());
        log.info("退款发起成功,refund:{}", refund);

    }

    @Override
    public RefundNotification notifyRefund(String requestBody, String signature, String serial, String nonc, String wechatTimestamp, String signType) {
        com.wechat.pay.java.core.notification.RequestParam requestParam = new com.wechat.pay.java.core.notification.RequestParam.Builder()
                .serialNumber(serial)
                .nonce(nonc)
                .signature(signature)
                .signType(signType)
                .timestamp(wechatTimestamp)
                .body(requestBody)
                .build();

        NotificationParser parser = new NotificationParser(notificationConfig);
        // 以支付通知回调为例，验签、解密并转换成 Transaction
        // 退款是RefundNotification
        RefundNotification transaction = parser.parse(requestParam, RefundNotification.class);
        log.info("微信退款回调生效" + JSON.toJSONString(transaction));
        return transaction;

    }

    @Override
    public byte[] qrCodePic(String scene) {
        // 获取 access token
        String accessToken = getAccessToken();

        // 微信小程序码API的URL
        String url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + accessToken;

        // 创建请求体，包括 env_version 参数
        String jsonPayload = String.format(
                "{\"page\":\"%s\", \"scene\":\"%s\", \"env_version\":\"%s\"}",
                "pages/index/index", scene, version
        );

        // 构建请求
        RequestBody body = RequestBody.create(
                jsonPayload,
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        // 执行请求并获取响应
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                // 返回二维码图片的字节数组
                return response.body().bytes();
            } else {
                // 处理错误，返回空字节数组或抛出异常
                throw new ApiException("Failed to generate QR code: " + response.message());
            }
        } catch (Exception e) {
            // 处理异常
            throw new ApiException("Error while generating QR code", e);
        }
    }

    @Override
    public Transaction notifyPay(String requestBody, String signature, String serial, String nonc, String wechatTimestamp, String signType) {
        com.wechat.pay.java.core.notification.RequestParam requestParam = new com.wechat.pay.java.core.notification.RequestParam.Builder()
                .serialNumber(serial)
                .nonce(nonc)
                .signature(signature)
                .signType(signType)
                .timestamp(wechatTimestamp)
                .body(requestBody)
                .build();

        NotificationParser parser = new NotificationParser(notificationConfig);
        // 以支付通知回调为例，验签、解密并转换成 Transaction
        // 退款是RefundNotification
        Transaction transaction = parser.parse(requestParam, Transaction.class);
        log.info("微信回调生效" + JSON.toJSONString(transaction));
        return transaction;
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
            String string = response.body().string();
            log.info("get Phone response{}", string);
            // 获取响应体内容
            JSONObject entries = new JSONObject(string);
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
            if (!StringUtils.equals(version, "release")) {
                throw new ApiException("当前环境不能获取accessToken，请从生产环境复制下来");
            }
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
