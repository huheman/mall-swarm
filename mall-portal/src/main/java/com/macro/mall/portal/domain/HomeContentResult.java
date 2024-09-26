package com.macro.mall.portal.domain;

import com.macro.mall.model.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * 首页内容返回信息封装
 * Created by macro on 2019/1/28.
 */
@Getter
@Setter
public class HomeContentResult {
    //轮播广告
    private List<SmsHomeAdvertise> advertiseList;
    // 用productCategory作为game
    private List<PmsProductCategory> gameList;
    //推荐品牌
    private List<PmsBrand> brandList;
    //当前秒杀场次
    private HomeFlashPromotion homeFlashPromotion;
    //新品推荐
    private List<PmsProduct> newProductList;
    //人气推荐
    private List<PmsProduct> hotProductList;
    //推荐专题
    private List<CmsSubject> subjectList;

    private Long orderCount;
    private Long userCount;
    /*最近的9张订单*/
    private List<ResultUnit> lastBuyList;

    public String getOrderCountStr() {
        if (orderCount == null) {
            return "";
        }
        BigDecimal result = new BigDecimal(orderCount);
        String unit = "";
        if (orderCount.compareTo(10000L) >= 0) {
            result = result.divide(new BigDecimal("10000"), 2, BigDecimal.ROUND_HALF_UP);
            unit = "万";
        }

        return result.stripTrailingZeros().toPlainString() + unit;
    }


    public String getUserCountStr() {
        if (userCount == null) {
            return "";
        }
        BigDecimal result = new BigDecimal(userCount);
        String unit = "";
        if (userCount.compareTo(10000L) >= 0) {
            result = result.divide(new BigDecimal("10000"), 2, BigDecimal.ROUND_HALF_UP);
            unit = "万";
        }

        return result.stripTrailingZeros().toPlainString() + unit;
    }


    @Data
    public static class ResultUnit {
        private String userName;
        private String gameName;
        private BigDecimal money;
        private Date buyTime;
    }
}
