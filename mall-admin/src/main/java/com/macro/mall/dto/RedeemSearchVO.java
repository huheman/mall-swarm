package com.macro.mall.dto;

import lombok.Data;

@Data
public class RedeemSearchVO {
    private String kolId;
    private Long gameId;
    private String useStatus;
    private Integer pageNum;
    private Integer pageSize;
}