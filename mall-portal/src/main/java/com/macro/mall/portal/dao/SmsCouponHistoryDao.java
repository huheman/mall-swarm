package com.macro.mall.portal.dao;

import com.macro.mall.model.SmsCoupon;
import com.macro.mall.portal.domain.SmsCouponHistoryDetail;
import com.macro.mall.portal.service.bo.CouponReceiveBO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 会员优惠券领取历史自定义Dao
 * Created by macro on 2018/8/29.
 */
public interface SmsCouponHistoryDao {
    List<SmsCouponHistoryDetail> getDetailList(@Param("memberId") Long memberId);
    List<SmsCoupon> getCouponList(@Param("memberId") Long memberId, @Param("useStatus")Integer useStatus);

    List<CouponReceiveBO> countHistory(@Param("couponIds")List<Long> couponIds, @Param("memberId")Long memberId);
}
