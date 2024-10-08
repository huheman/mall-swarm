package com.macro.mall.portal.controller;

import cn.hutool.core.lang.Assert;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.common.exception.ApiException;
import com.macro.mall.model.SmsCoupon;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.domain.CartPromotionItem;
import com.macro.mall.portal.domain.SmsCouponHistoryDetail;
import com.macro.mall.portal.service.OmsCartItemService;
import com.macro.mall.portal.service.UmsMemberCouponService;
import com.macro.mall.portal.service.UmsMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 会员优惠券管理Controller
 * Created by macro on 2018/8/29.
 */
@Controller
@Tag(name = "UmsMemberCouponController", description = "用户优惠券管理")
@RequestMapping("/member/coupon")
public class UmsMemberCouponController {
    private static final Logger log = LoggerFactory.getLogger(UmsMemberCouponController.class);
    @Autowired
    private UmsMemberCouponService memberCouponService;
    @Autowired
    private OmsCartItemService cartItemService;
    @Autowired
    private UmsMemberService memberService;

    @Operation(summary = "领取指定优惠券")
    @RequestMapping(value = "/add/{couponId}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult add(@PathVariable Long couponId) {
        memberCouponService.add(couponId, memberService.getCurrentMember().getId(), 1);
        return CommonResult.success(null, "领取成功");
    }

    @Operation(summary = "暴露API使用的领券")
    @RequestMapping(value = "/addByApi", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult addByApi(@RequestParam Long couponId, @RequestParam String phone) {
        UmsMember member = memberService.getByPhone(phone);
        if (member == null) {
            throw new ApiException("未找到" + phone + "对应的用户");
        }
        memberCouponService.add(couponId, member.getId(), 0);
        return CommonResult.success(null, "领取成功");
    }


    @Operation(summary = "获取会员优惠券列表")
    @Parameter(name = "useStatus", description = "优惠券筛选类型:0->未使用；1->已使用；2->已过期",
            in = ParameterIn.QUERY, schema = @Schema(type = "integer", allowableValues = {"0", "1", "2"}))
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<SmsCoupon>> list(@RequestParam(value = "useStatus", required = false) Integer useStatus) {
        List<SmsCoupon> couponList = memberCouponService.list(useStatus);
        return CommonResult.success(couponList);
    }

    @Operation(summary = "获取登录会员购物车的相关优惠券")
    @Parameter(name = "type", description = "使用可用:0->不可用；1->可用",
            in = ParameterIn.PATH, schema = @Schema(type = "integer", defaultValue = "1", allowableValues = {"0", "1"}))
    @RequestMapping(value = "/list/cart/{type}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<SmsCouponHistoryDetail>> listCart(@PathVariable Integer type) {
        List<CartPromotionItem> cartPromotionItemList = cartItemService.listPromotion(memberService.getCurrentMember().getId(), null);
        List<SmsCouponHistoryDetail> couponHistoryList = memberCouponService.listCart(cartPromotionItemList, type);
        return CommonResult.success(couponHistoryList);
    }

    @Operation(summary = "获取当前商品相关优惠券")
    @RequestMapping(value = "/listByProduct/{productId}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<SmsCoupon>> listByProduct(@PathVariable Long productId) {
        List<SmsCoupon> couponHistoryList = memberCouponService.listByProduct(productId);
        return CommonResult.success(couponHistoryList);
    }

    @GetMapping("/listByMember")
    @ResponseBody
    public CommonResult<List<SmsCoupon>> listByMember() {
        UmsMember currentMember = null;
        try {
            currentMember = memberService.getCurrentMember();
        } catch (Exception e) {
            log.error("获取当前登陆人失败", e);
        }
        if (currentMember == null) {
            return CommonResult.success(Collections.EMPTY_LIST);
        }
        List<SmsCoupon> smsCoupons = memberCouponService.listByMember(currentMember.getId(), 0);
        return CommonResult.success(smsCoupons);
    }
}
