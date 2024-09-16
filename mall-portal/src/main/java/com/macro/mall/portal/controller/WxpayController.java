package com.macro.mall.portal.controller;

import cn.hutool.json.JSONObject;
import com.macro.mall.common.api.CommonResult;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.core.notification.Notification;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

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
    public static String merchantId = "1685896334";
    /**
     * 商户API私钥路径
     */
    public static String privateKeyPath = "D:/env/cert/apiclient_key.pem";
    /**
     * 商户证书序列号
     */
    public static String merchantSerialNumber = "12A2CC33B64631EFB1FC18E49126848CFF8B9D2D";
    /**
     * 商户APIV3密钥
     */
    public static String apiV3Key = "enjrxNAC2R9MdC92YpEJziJ3hgZmG7Yd";
    public static String appId = "wxe26bc51aa1206df9";
    //    private static String openId = "oZdSX7VaJfp6c_X-G0K2cpHCLebw";
    private static final String APP_SECRET = "9cfe3edcea819785c8e355b540fdcffa";


    /*微信下单分为两步。第一步获取replay_id，小程序通过repay_id调起小程序支付模块进行付款*/
    @GetMapping("/createOrder")
    public CommonResult<PrepayWithRequestPaymentResponse> createOrder(String openId) {
        // 使用自动更新平台证书的RSA配置
        // 一个商户号只能初始化一个配置，否则会因为重复的下载任务报错
        Config config =
                new RSAAutoCertificateConfig.Builder()
                        .merchantId(merchantId)
                        .privateKeyFromPath(privateKeyPath)
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
                appId, APP_SECRET, code
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
    public ResponseEntity.BodyBuilder notify(HttpServletRequest request) {
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
                .privateKeyFromPath(privateKeyPath)
                .merchantSerialNumber(merchantSerialNumber)
                .apiV3Key(apiV3Key)
                .build();
        NotificationParser parser = new NotificationParser(config);
        try {
            // 以支付通知回调为例，验签、解密并转换成 Transaction
            Notification transaction = parser.parse(requestParam, Notification.class);
            log.info("微信回调生效");
        } catch (ValidationException e) {
            // 签名验证失败，返回 401 UNAUTHORIZED 状态码
            log.error("sign verification failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED);
        }

        // 处理成功，返回 200 OK 状态码
        return ResponseEntity.status(HttpStatus.OK);

    }


}
