package com.macro.mall.dto;

import lombok.Data;

@Data
public class OrderInfoDTO {
    // 下单量
    private Long orderCount;
    // 成交订单量
    private Long finishOrderCount;
    // 成交金额
    private Double orderAmount;
    // 下单人数
    private Long userCount;
    // 成交人数
    private Long finishUserCount;
    // kolId
    private String kolId;
}
