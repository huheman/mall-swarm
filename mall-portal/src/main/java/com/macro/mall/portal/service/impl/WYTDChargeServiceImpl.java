package com.macro.mall.portal.service.impl;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSONObject;
import com.macro.mall.portal.service.WYTDChargeService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

@Component
@Slf4j
public class WYTDChargeServiceImpl implements WYTDChargeService {
    @Value("${wytd.userKey}")
    private String userKey;

    @Value("${wytd.userId}")
    private String userId;

    @Autowired
    private OkHttpClient okHttpClient;

    @SneakyThrows
    public String generateSignature(JSONObject jsonObject) {
        String apiKey = userKey;
        // 1. 使用TreeMap按键名进行升序排序
        Map<String, Object> sortedMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        sortedMap.putAll(jsonObject);

        // 2. 拼接键值对字符串，忽略Sign字段
        StringBuilder srcBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : sortedMap.entrySet()) {
            srcBuilder.append(entry.getKey().toLowerCase()).append(entry.getValue());
        }

        // 3. 拼接apiKey
        srcBuilder.append("key").append(apiKey);

        // 4. 使用GBK编码进行MD5加密
        String src = srcBuilder.toString();
        return md5(src, "GBK");
    }

    @Override
    public String status(String orderSN) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = dateFormat.format(date);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("UserOrderId", orderSN);
        jsonObject.put("UserId", userId);
        jsonObject.put("TimeStamp", timestamp);
        String signature = generateSignature(jsonObject);
        jsonObject.put("Sign", signature);
        String url = "http://open.greatnesss.com/api/Order/QueryChargeCardsResult";
        RequestBody requestBody = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        // 发送请求并获取响应
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalArgumentException("response code: " + response.code());
            }
            // 解析响应体
            String responseBody = response.body().string();
            return responseBody;
        } catch (Exception e) {
            log.error("查询失败", e);
            throw new RuntimeException(e);
        }
    }


    // 生成 encKey
    private static String generateEncKey(String orderId, String key) throws Exception {
        String srcKey = orderId + key;
        // 使用 MD5 算法生成 32 位的 srcKey
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] srcKeyBytes = md.digest(srcKey.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : srcKeyBytes) {
            sb.append(String.format("%02x", b));
        }
        // 截取前16位作为 encKey
        return sb.toString().substring(0, 16);
    }

    // AES 解密
    private static String aesDecrypt(String str, String password) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(password.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decoded = Base64.decodeBase64(str);
        byte[] original = cipher.doFinal(decoded);
        return new String(original, StandardCharsets.UTF_8);
    }

    // GZIP 解压缩
    private static String gzipDecompress(String str) throws Exception {
        byte[] compressed = Base64.decodeBase64(str);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(compressed);
        GZIPInputStream gzip = new GZIPInputStream(in);
        byte[] buffer = new byte[1024];
        int n;
        while ((n = gzip.read(buffer)) >= 0) {
            out.write(buffer, 0, n);
        }
        return out.toString("UTF-8");
    }

    // 主解密方法
    public String decryptCards(String wytdOrderId, String cardsStr) {
        try {
            // 1. 生成解密密钥 encKey
            String encKey = generateEncKey(wytdOrderId, userKey);

            // 2. AES 解密得到压缩的卡信息
            String compressCards = aesDecrypt(cardsStr, encKey);

            // 3. 解压缩卡信息
            String cardsInfo = gzipDecompress(compressCards);

            // 4. 返回解密后的卡信息
            return cardsInfo;
        } catch (Exception e) {
            log.error("解密失败", e);
            return null;
        }
    }

    private String md5(String src, String charset) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] bytes = md.digest(src.getBytes(charset));

        // 转换为32位MD5字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public void createOrder(Long goodsId, Integer buyNum, String gameArea, String gameServer, String chargeAccount, String userOrderId) throws Exception {
        String url = "http://open.greatnesss.com/api/Order/CreateGameOrder";
        Assert.notNull(goodsId, "goodsId can not be null");
        Assert.notNull(buyNum, "buyNum can not be null");
        Assert.notNull(userOrderId, "userOrderId can not be null");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("GoodsId", goodsId);
        jsonObject.put("BuyNum", buyNum);
        if (StringUtils.isEmpty(chargeAccount)) {
            chargeAccount = "kami";
        }
        jsonObject.put("ChargeAccount", chargeAccount);
        jsonObject.put("UserOrderId", userOrderId);
        jsonObject.put("UserId", userId);

        if (StringUtils.isNoneEmpty(gameArea)) {
            jsonObject.put("GameArea", gameArea);
        }
        if (StringUtils.isNoneEmpty(gameServer)) {
            jsonObject.put("GameServer", gameServer);
        }

        String signature = generateSignature(jsonObject);
        jsonObject.put("Sign", signature);
        RequestBody requestBody = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        // 发送请求并获取响应
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalArgumentException("response code: " + response.code());
            }
            // 解析响应体
            String responseBody = response.body().string();
            JSONObject resp = JSONObject.parseObject(responseBody);
            Integer code = resp.getInteger("Code");
            Assert.state(1 == code, "直充失败了:" + resp.getString("Message"));
            log.info(responseBody);
        } catch (Exception e) {
            log.error("直充失败", e);
            throw e;
        }
    }
}
