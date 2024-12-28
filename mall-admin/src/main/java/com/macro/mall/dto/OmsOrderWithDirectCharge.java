package com.macro.mall.dto;

import com.macro.mall.model.OmsOrder;
import lombok.Data;

@Data
public class OmsOrderWithDirectCharge extends OmsOrder {
    private Integer directChargeStatus;
    private String directChargeFailReason;
    private String firstInviteKol;
    private String directChargeDetail;
}
