package com.macro.mall.dto;

import com.macro.mall.model.RedeemCodeRecord;
import lombok.Data;

@Data
public class FullRedeemCodeRecordDTO extends RedeemCodeRecord {
    private String gameName;
    private String productName;
    private String skuName;
}
