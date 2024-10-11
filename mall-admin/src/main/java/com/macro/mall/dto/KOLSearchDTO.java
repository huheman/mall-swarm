package com.macro.mall.dto;

import lombok.Data;

import java.sql.Date;

@Data
public class KOLSearchDTO {
    private Integer pageNum;
    private Integer pageSize;
    private String kolName;
    private String kolId;
    private Date startTime;
    private Date endTime;
}
