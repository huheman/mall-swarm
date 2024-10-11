package com.macro.mall.dto;

import com.macro.mall.model.SmsKolPromoConfig;
import lombok.Data;

@Data
public class KOLInfoDTO extends SmsKolPromoConfig {
    private Long orderCount;
    private Long finishOrderCount;
    private Double orderAmount;
    private Long userCount;
    private Long finishUserCount;
}
