package com.macro.mall.service;

import com.macro.mall.mapper.RedeemCodeRecordMapper;
import com.macro.mall.model.RedeemCodeRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class RedeemService {
    @Autowired
    private RedeemCodeRecordMapper redeemCodeRecordMapper;
    // 可用的字符集，去掉了容易混淆的字符
    private static final String CHAR_POOL = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();


    private String generateCode(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHAR_POOL.length());
            code.append(CHAR_POOL.charAt(index));
        }
        return code.toString();
    }

    @Async
    public void generateRedeemCode(Integer generateCount, Long skuId, String kolId) {
        Assert.notNull(generateCount, "generateCount is empty");
        Assert.notNull(skuId, "skuId is empty");
        Assert.hasLength(kolId, "kolId is empty");
        Assert.state(generateCount > 0, "generateCount must be greater than 0");
        List<RedeemCodeRecord> result = new ArrayList<>();
        for (int i = 0; i < generateCount; i++) {
            RedeemCodeRecord record = new RedeemCodeRecord();
            record.setRedeemCode(generateCode(24));
            record.setSkuId(skuId);
            record.setKolId(kolId);
            record.setCreateTime(new Date());
            record.setUseStatus("NOT_USED");
            result.add(record);
        }
        for (RedeemCodeRecord redeemCodeRecord : result) {
            try {
                redeemCodeRecordMapper.insertSelective(redeemCodeRecord);

            } catch (Exception e) {
                log.error("插入失败", e);
            }
        }
    }
}
