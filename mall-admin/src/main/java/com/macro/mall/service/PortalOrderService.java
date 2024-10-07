package com.macro.mall.service;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.common.exception.ApiException;
import com.macro.mall.dto.OmsOrderDeliveryParam;
import com.macro.mall.model.UmsMember;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("mall-portal")
public interface PortalOrderService {
    @PostMapping(value = "/order/confirmReceiveOrder")
    CommonResult confirmReceiveOrder(@RequestParam("orderId")Long orderId);

    @GetMapping("/wxpay/ship")
    CommonResult<Boolean> ship(@RequestParam("orderId")Long orderId);

    @PostMapping("/order/refund")
    CommonResult<String> refund(@RequestParam("orderId") Long id,@RequestParam("reason")String reason);

    @PostMapping("/order/close")
    CommonResult<Integer> closeOrder(@RequestParam("orderId") Long orderId, @RequestParam("operator") String operator, @RequestParam("reason") String reason);

    @RequestMapping(value = "/member/coupon/addByApi", method = RequestMethod.POST)
    CommonResult addByApi(@RequestParam Long couponId, @RequestParam String phone);
}
