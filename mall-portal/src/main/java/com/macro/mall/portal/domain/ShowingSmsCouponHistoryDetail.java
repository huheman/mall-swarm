package com.macro.mall.portal.domain;

import lombok.Data;

/**
 * 优惠券领取历史详情封装
 * Created by macro on 2018/8/29.
 */
@Data
public class ShowingSmsCouponHistoryDetail extends SmsCouponHistoryDetail {
    private Boolean canUse;

    public ShowingSmsCouponHistoryDetail(SmsCouponHistoryDetail smsCouponHistoryDetail) {
        setCoupon(smsCouponHistoryDetail.getCoupon());
    }
}
