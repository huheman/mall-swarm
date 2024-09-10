package com.macro.mall.portal.util;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class AESUtil {
    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 16; // 128位, 如果你想要192位或256位可以改为24或32

    // Padding 密钥到指定长度
    private static byte[] padSecretKey(String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length == KEY_SIZE) {
            return keyBytes;
        }
        byte[] paddedKey = new byte[KEY_SIZE];
        // 如果密钥长度超过了指定长度，截取前面的部分
        if (keyBytes.length > KEY_SIZE) {
            System.arraycopy(keyBytes, 0, paddedKey, 0, KEY_SIZE);
        } else {
            // 如果密钥长度不足，填充0
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            Arrays.fill(paddedKey, keyBytes.length, KEY_SIZE, (byte) 0);
        }
        return paddedKey;
    }

    @SneakyThrows
    public static String encrypt(String content, String secretKey) {
        if (StringUtils.isEmpty(content)) {
            return content;
        }
        byte[] keyBytes = padSecretKey(secretKey);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encrypted = cipher.doFinal(content.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    @SneakyThrows
    public static String decrypt(String encryptedContent, String secretKey) {
        if (StringUtils.isEmpty(encryptedContent)) {
            return encryptedContent;
        }
        byte[] keyBytes = padSecretKey(secretKey);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] original = cipher.doFinal(Base64.getDecoder().decode(encryptedContent));
        return new String(original);
    }
}
