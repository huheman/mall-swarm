package com.macro.mall.portal.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.macro.mall.portal.service.YZJChargeService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class YZJChargeServiceImpl implements YZJChargeService {
    @Autowired
    private OkHttpClient okHttpClient;
    // http://121.40.119.11/api/fop-order/create
    @Value("${yzj.url}")
    private String url;
    // dXBW1ozLC3jmT2lbI4NGecfSswUbAEms
    @Value("${yzj.appKey}")
    private String appKey;
    // 1948717266@qq.com
    @Value("${yzj.email}")
    private String email;

    @Value("${yzj.noticeUrl}")
    private String noticeUrl;

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String CHARSET_NAME = "UTF-8";

    @Override
    public void createOrder(Long goodsId, Integer buyNum, String gameArea, String gameServer, String chargeAccount, String userOrderId) throws Exception {
        // 获取当前时间
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        // 创建订单的data参数内容
        Map<String, Object> data = new HashMap<>();
        data.put("time", currentTime);
        data.put("product_id", goodsId);
        data.put("quantity", buyNum);
        data.put("charge_account", chargeAccount);
        data.put("charge_region", gameArea);
        data.put("charge_server", gameServer);
        data.put("customer_order_id", userOrderId);
        data.put("api_notify_url", noticeUrl);

        // 加密data，假设encryptData是一个加密方法
        String encryptedData = encryptData(data);

        // 发送POST请求
        String postData = String.format("{\"email\":\"%s\",\"data\":\"%s\"}", email, encryptedData);

        String apiUrl = url;

        // 假设sendPostRequest是发送HTTP POST请求的方法
        String response = sendPostRequest(apiUrl, postData);
        log.info("咔之家直充结果{}", response);
        // 处理响应
        // 假设decryptData是解密方法，解析响应data
        JSONObject responseData = JSONObject.parseObject(response);

        // 检查返回的状态
        if (responseData.getInteger("status").equals(1)) {
            log.info("订单创建成功: " + decryptData(responseData.getString("data")));
        } else {
            throw new Exception("订单创建失败: " + responseData.get("message"));
        }
    }

    // AES加密
    private String encryptData(Map<String, Object> data) throws Exception {
        // 将数据转换为JSON字符串
        String jsonData = new JSONObject(data).toString();

        // 获取API密钥，作为加密的Key
        String apiKey = appKey;
        Key secretKey = new SecretKeySpec(apiKey.getBytes(StandardCharsets.UTF_8), "AES");

        // 生成随机16字节IV
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // 配置AES加密模式
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        // 加密数据
        byte[] encryptedData = cipher.doFinal(jsonData.getBytes(StandardCharsets.UTF_8));

        // 将加密的IV和数据分别Base64编码
        String encodedIv = Base64.getEncoder().encodeToString(iv);
        String encodedEncryptedData = Base64.getEncoder().encodeToString(encryptedData);

        // 构造最终的加密JSON字符串
        JSONObject encryptedJson = new JSONObject();
        encryptedJson.put("iv", encodedIv);
        encryptedJson.put("value", encodedEncryptedData);

        // 将整个加密的JSON对象再次Base64编码为最终的密文
        return Base64.getEncoder().encodeToString(encryptedJson.toString().getBytes(StandardCharsets.UTF_8));
    }


    private String sendPostRequest(String url, String postData) throws Exception {
        // 构造POST请求体
        RequestBody body = RequestBody.create(postData, MediaType.parse("application/json; charset=utf-8"));

        // 创建Request对象
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        // 发送请求并获取响应
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                throw new Exception("HTTP请求失败，状态码：" + response.code());
            }
        }
    }


    // AES解密
    @SneakyThrows
    public Map<String, Object> decryptData(String encryptedData) {
        // 将Base64编码的密文解码为JSON对象
        String decodedData = new String(Base64.getDecoder().decode(encryptedData), StandardCharsets.UTF_8);
        JSONObject encryptedJson = JSONObject.parseObject(decodedData);

        // 获取Base64编码的IV和加密的数据
        String encodedIv = encryptedJson.getString("iv");
        String encodedValue = encryptedJson.getString("value");

        // 解码IV和加密数据
        byte[] iv = Base64.getDecoder().decode(encodedIv);
        byte[] encryptedValue = Base64.getDecoder().decode(encodedValue);

        // 获取API密钥，作为解密的Key
        String apiKey = appKey;
        Key secretKey = new SecretKeySpec(apiKey.getBytes(StandardCharsets.UTF_8), "AES");

        // 配置AES解密模式
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        // 解密数据
        byte[] decryptedData = cipher.doFinal(encryptedValue);

        // 将解密后的数据转换为JSON对象
        String decryptedJsonString = new String(decryptedData, StandardCharsets.UTF_8);
        JSONObject decryptedJson = JSONObject.parseObject(decryptedJsonString);

        // 将JSON对象转换为Map
        return decryptedJson.getInnerMap();
    }
}
