package com.macro.mall.portal.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.common.service.RedisService;
import com.macro.mall.common.service.impl.RedisServiceImpl;
import com.macro.mall.portal.service.UmsMemberService;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.RequestBody;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "WxpayController", description = "微信支付相关接口")
@RequestMapping("/wxpay")
@Slf4j
public class WxpayController {
    @Autowired
    private OkHttpClient okHttpClient;
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
    //    private static String openId = "oZdSX7VaJfp6c_X-G0K2cpHCLebw";
    @Value("${wx.appSecret}")
    private String appSecret;

    @Value("${sa-token.token-prefix}")
    private String tokenHead;
    @Autowired
    private RedisService redisService;
    @Autowired
    private UmsMemberService umsMemberService;


    /*微信下单分为两步。第一步获取replay_id，小程序通过repay_id调起小程序支付模块进行付款*/
    @GetMapping("/createOrder")
    public CommonResult<PrepayWithRequestPaymentResponse> createOrder(String openId) {
        // 使用自动更新平台证书的RSA配置
        // 一个商户号只能初始化一个配置，否则会因为重复的下载任务报错
        Config config =
                new RSAAutoCertificateConfig.Builder()
                        .merchantId(merchantId)
                        .privateKey(privateKey)
                        .merchantSerialNumber(merchantSerialNumber)
                        .apiV3Key(apiV3Key)
                        .build();
        // 构建service
        JsapiServiceExtension service = new JsapiServiceExtension.Builder().config(config).build();
        // request.setXxx(val)设置所需参数，具体参数可见Request定义
        // 跟之前下单示例一样，填充预下单参数
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        amount.setTotal(1);
        request.setAmount(amount);
        request.setAppid(appId);
        request.setMchid(merchantId);
        request.setDescription("测试商品标题");
        request.setNotifyUrl("https://www.huhp.cc/api/mall-portal/wxpay/notify");
        Payer payer = new Payer();
        payer.setOpenid(openId);
        request.setPayer(payer);
        request.setOutTradeNo(RandomStringUtils.randomAlphabetic(20));
        PrepayWithRequestPaymentResponse response = service.prepayWithRequestPayment(request);
        System.out.println(response);
        return CommonResult.success(response);
    }

    @GetMapping("getOpenId")
    @SneakyThrows
    public CommonResult getOpenId(@RequestParam String code) {
        // 拼接微信接口的 URL
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, code
        );

        // 构建 OkHttp 请求
        Request request = new Request.Builder()
                .url(url)
                .build();

        // 发送请求并获取响应
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalArgumentException("ok");
            }

            // 解析响应体
            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);
            return CommonResult.success(jsonResponse);
        }
    }

    @SneakyThrows
    @PostMapping("notify")
    @ResponseBody
    public CommonResult<String> notify(HttpServletRequest request) {
        String signature = request.getHeader("Wechatpay-Signature");
        String serial = request.getHeader("Wechatpay-Serial");
        String nonc = request.getHeader("Wechatpay-Nonce");
        String wechatTimestamp = request.getHeader("Wechatpay-Timestamp");
        String signType = request.getHeader("Wechatpay-Signature-Type");
        byte[] bytes = request.getInputStream().readAllBytes();
        String requestBody = new String(bytes, StandardCharsets.UTF_8);
        com.wechat.pay.java.core.notification.RequestParam requestParam = new com.wechat.pay.java.core.notification.RequestParam.Builder()
                .serialNumber(serial)
                .nonce(nonc)
                .signature(signature)
                .signType(signType)
                .timestamp(wechatTimestamp)
                .body(requestBody)
                .build();
        NotificationConfig config = new RSAAutoCertificateConfig.Builder()
                .merchantId(merchantId)
                .privateKey(privateKey)
                .merchantSerialNumber(merchantSerialNumber)
                .apiV3Key(apiV3Key)
                .build();
        NotificationParser parser = new NotificationParser(config);
        try {
            // 以支付通知回调为例，验签、解密并转换成 Transaction
            Transaction transaction = parser.parse(requestParam, Transaction.class);
            log.info("微信回调生效" + JSON.toJSONString(transaction));
            log.info("微信回调生效,outTradeNo是" + transaction.getOutTradeNo());
        } catch (ValidationException e) {
            // 签名验证失败，返回 401 UNAUTHORIZED 状态码
            log.error("sign verification failed", e);
            return CommonResult.failed(e.getMessage());
        }

        // 处理成功，返回 200 OK 状态码
        return CommonResult.success("ok");

    }

    /*https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-info/phone-number/getPhoneNumber.html*/
    @GetMapping("loginByWxPhoneCode")
    @ResponseBody
    public CommonResult getPhoneNum(String code) {
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
            String phone = entries.getJSONObject("phone_info").getStr("purePhoneNumber");
            SaTokenInfo saTokenInfo = umsMemberService.loginByPhone(phone);
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("token", saTokenInfo.getTokenValue());
            tokenMap.put("tokenHead", tokenHead + " ");
            return CommonResult.success(tokenMap);
        } catch (IOException e) {
            e.printStackTrace();
            return CommonResult.failed("请求失败: " + e.getMessage());
        }
    }


    /*https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-access-token/getAccessToken.html*/
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
                redisService.set("accessToken", accessToken,60*60);
                return accessToken;
            }
        }else {
            return accessToken;
        }
    }

}
