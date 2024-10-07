package com.macro.mall.controller;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.dto.SmsCouponHistoryDTO;
import com.macro.mall.service.PortalOrderService;
import com.macro.mall.service.SmsCouponHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 优惠券领取记录管理Controller
 * Created by macro on 2018/11/6.
 */
@Controller
@Tag(name = "SmsCouponHistoryController", description = "优惠券领取记录管理")
@RequestMapping("/couponHistory")
public class SmsCouponHistoryController {
    @Autowired
    private SmsCouponHistoryService historyService;
    @Autowired
    private PortalOrderService portalOrderService;

    @Operation(summary = "根据优惠券id，使用状态，订单编号分页获取领取记录")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<SmsCouponHistoryDTO>> list(@RequestParam(value = "couponId", required = false) Long couponId,
                                                              @RequestParam(value = "useStatus", required = false) Integer useStatus,
                                                              @RequestParam(value = "orderSn", required = false) String orderSn,
                                                              @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                              @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<SmsCouponHistoryDTO> historyList = historyService.list(couponId, useStatus, orderSn, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(historyList));
    }

    @Operation(summary = "手工赠券")
    @PostMapping("giveCoupon")
    @ResponseBody
    public CommonResult giveCoupon(@RequestParam Long couponId, @RequestParam String phone) {
        return portalOrderService.addByApi(couponId, phone);
    }
}
