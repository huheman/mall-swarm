package com.macro.mall.portal.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 生成订单时传入的参数
 * Created by macro on 2018/8/30.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OrderParam {
    @Schema(title = "收货地址ID")
    private Long memberReceiveAddressId;
    @Schema(title = "优惠券ID")
    private Long couponId;
    @Schema(title = "使用的积分数")
    private Integer useIntegration;
    @Schema(title = "支付方式")
    private Integer payType;
    @Schema(title = "被选中的购物车商品ID")
    private List<Long> cartIds;
    @Schema(title = "订单的标题")
    private String title;
    @Schema(title = "订单来源：0:网页订单；1->小程序订单，2: 微信H5订单")
    private Integer sourceType;
    @Schema(title = "kol来源")
    private String kolId;
    @Schema(title = "使用平台")
    private String platform;

}
