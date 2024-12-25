package com.macro.mall.portal.controller.vo;

import lombok.Data;

@Data
public class RedeemInfoVO {
    private String redeemCode;
    private Long productId;
    private Long skuId;
    private String kolId;
}
