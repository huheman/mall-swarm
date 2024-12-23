package com.macro.mall.portal.service;

import com.macro.mall.mapper.PmsSkuStockMapper;
import com.macro.mall.mapper.RedeemCodeRecordMapper;
import com.macro.mall.model.PmsSkuStock;
import com.macro.mall.model.RedeemCodeRecord;
import com.macro.mall.model.RedeemCodeRecordExample;
import com.macro.mall.portal.controller.vo.RedeemInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.security.SecureRandom;
import java.util.List;

@Service
@Slf4j
public class RedeemService {
    @Autowired
    private RedeemCodeRecordMapper redeemCodeRecordMapper;
    @Autowired
    private PmsSkuStockMapper skuStockMapper;
    // 可用的字符集，去掉了容易混淆的字符
    private static final String CHAR_POOL = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public void useRedeem(String redeemCode, Long skuId, String usePhone, String orderSN) {
        RedeemCodeRecord redeemCodeRecord = find(redeemCode);
        Assert.state("NOT_USED".equals(redeemCodeRecord.getUseStatus()),"此兑换券已经被使用过了");
        redeemCodeRecord.setUsePhone(usePhone);
        redeemCodeRecord.setSkuId(skuId);
        redeemCodeRecord.setUseStatus("HAS_USED");
        redeemCodeRecord.setUseOrderSn(orderSN);
        redeemCodeRecordMapper.updateByPrimaryKeySelective(redeemCodeRecord);
    }
    private RedeemCodeRecord find(String redeemCode) {
        Assert.hasLength(redeemCode, "redeemCode is empty");
        RedeemCodeRecordExample recordExample = new RedeemCodeRecordExample();
        recordExample.createCriteria().andRedeemCodeEqualTo(redeemCode);
        List<RedeemCodeRecord> redeemCodeRecords = redeemCodeRecordMapper.selectByExample(recordExample);
        Assert.notEmpty(redeemCodeRecords, "未找到此兑换码");
        return redeemCodeRecords.get(0);
    }

    public RedeemInfoVO info(String redeemCode) {
        RedeemCodeRecord redeemCodeRecord = find(redeemCode);
        Assert.notNull(redeemCodeRecord, "未找到此兑换码");
        Assert.state("NOT_USED".equals(redeemCodeRecord.getUseStatus()),"此兑换券已经被使用过了");
        PmsSkuStock pmsSkuStock = skuStockMapper.selectByPrimaryKey(redeemCodeRecord.getSkuId());
        RedeemInfoVO redeemInfoVO = new RedeemInfoVO();
        redeemInfoVO.setRedeemCode(redeemCode);
        redeemInfoVO.setProductId(pmsSkuStock.getProductId());
        redeemInfoVO.setSkuId(redeemCodeRecord.getSkuId());
        return redeemInfoVO;
    }
}


