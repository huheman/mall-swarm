package com.macro.mall.portal.config;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.http.DefaultHttpClientBuilder;
import com.wechat.pay.java.core.http.HttpClient;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.refund.RefundService;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WxPayConfig {
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


    @Bean
    public Config wxPayApiConfig() {
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(merchantId)
                .privateKey(privateKey)
                .merchantSerialNumber(merchantSerialNumber)
                .apiV3Key(apiV3Key)
                .build();
    }

    @Bean
    public HttpClient httpClient(Config config) {
        // 开启双域名重试，并关闭 OkHttp 默认的连接失败后重试
        return new DefaultHttpClientBuilder()
                .config(config)
                .disableRetryOnConnectionFailure()
                .enableRetryMultiDomain()
                .build();
    }

    @Bean
    public JsapiServiceExtension jsapiServiceExtension(HttpClient httpClient,Config config) {
        // 构建service
        return new JsapiServiceExtension.Builder().httpClient(httpClient).config(config).build();
    }

    @Bean
    public RefundService refundService(HttpClient httpClient,Config config) {
        return new RefundService.Builder().httpClient(httpClient).config(config).build();
    }

    @Bean
    public NotificationConfig notificationConfig() {
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(merchantId)
                .privateKey(privateKey)
                .merchantSerialNumber(merchantSerialNumber)
                .apiV3Key(apiV3Key)
                .build();
    }

}
