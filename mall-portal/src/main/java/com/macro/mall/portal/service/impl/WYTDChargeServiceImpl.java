package com.macro.mall.portal.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSONObject;
import com.macro.mall.portal.service.WYTDChargeService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

    private byte[] decode(String base64EncodedString) throws Exception {
        return Base64.decode(base64EncodedString);
    }

    private String base64UnCompress(String str) {

        byte[] zippedData = Base64.decode(str);

        return uncompressToString(zippedData);
    }

    private String uncompressToString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[1024];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toString("UTF-8");
        } catch (Exception e) {
            log.error("解压卡密失败", e);
            throw new RuntimeException(e);
        }
    }


    @Override
    public String decript(String cards) {
        if (org.springframework.util.StringUtils.isEmpty(cards)) {
            return "";
        }
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(userKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            String strTmp = new String(cipher.doFinal(decode(cards)));
            // 拿到后解压
            return base64UnCompress(strTmp);
        } catch (Exception ex) {
            log.error("解密cards失败", ex);
            throw new RuntimeException(ex);
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
