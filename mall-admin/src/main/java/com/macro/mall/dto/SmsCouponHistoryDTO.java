package com.macro.mall.dto;

import com.macro.mall.model.SmsCouponHistory;
import lombok.Data;

@Data
public class SmsCouponHistoryDTO extends SmsCouponHistory {
    private String phone;

}
