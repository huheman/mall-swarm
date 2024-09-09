package com.macro.mall.portal.service;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Component
public class DirectChargeServiceImpl implements DirectChargeService{
    private static final byte[] DIGITS = new byte[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static final String SIGNATURE_RAW_TEMPLATE = "%s@%s";

    @SneakyThrows
    public static String generateSignature(String appSecret, String timestampStr, TreeMap<String, String> sortedParams) throws NoSuchAlgorithmException {
        StringBuilder queryStringBuilder = new StringBuilder();
        Set<Map.Entry<String, String>> entries = sortedParams.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            queryStringBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        String signatureRaw = String.format(SIGNATURE_RAW_TEMPLATE, queryStringBuilder.substring(0, queryStringBuilder.length() - 1), timestampStr);

        SecretKeySpec secretKeySpec = new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);
        byte[] signatureBytes = mac.doFinal(signatureRaw.getBytes(StandardCharsets.UTF_8));

        return encode(signatureBytes);
    }

    private static String encode(byte[] data) {
        int len = data.length;
        byte[] out = new byte[len << 1];
        for (int i = 0, j = 0; i < len; i++) {
            out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS[0xF & data[i]];
        }
        return new String(out, StandardCharsets.UTF_8);
    }
    /*把订单直充*/
    @Override
    public void directCharge(Long orderId) {

    }
}
