package com.macro.mall.portal.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class DirectChargeDomain {
    private Long id;
    private String orderSN;
    /*1:充值中，2：充值成功,3:充值失败*/
    private Integer chargeStatus;
    private Timestamp createTime;
    private Long orderId;
    private String failReason;


    public DirectChargeDomain(String orderSN, Long orderId) {
        this.orderId= orderId;
        this.orderSN = orderSN;
        this.chargeStatus = 1;
        this.createTime = new Timestamp(System.currentTimeMillis());
    }



    public void success() {
        this.chargeStatus = 2;
        this.failReason = "";
    }

    public void fail(String failReason) {
        this.chargeStatus = 3;
        this.failReason = StringUtils.substring(failReason,0, 300);
    }
}
