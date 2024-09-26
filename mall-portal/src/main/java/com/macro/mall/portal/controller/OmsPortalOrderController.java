package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.domain.ConfirmOrderResult;
import com.macro.mall.portal.domain.OmsOrderDetail;
import com.macro.mall.portal.domain.OrderParam;
import com.macro.mall.portal.domain.OrderParamWithAttribute;
import com.macro.mall.portal.service.IdentityService;
import com.macro.mall.portal.service.OmsPortalOrderService;
import com.macro.mall.portal.service.UmsMemberService;
import com.macro.mall.portal.service.bo.IdentityResultBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * 订单管理Controller
 * Created by macro on 2018/8/30.
 */
@Slf4j
@Controller
@Tag(name = "OmsPortalOrderController", description = "订单管理")
@RequestMapping("/order")
public class OmsPortalOrderController {
    @Autowired
    private OmsPortalOrderService portalOrderService;
    @Autowired
    private UmsMemberService memberService;
    @Autowired
    private IdentityService identityService;


    /*查看卡密*/
    @GetMapping("/showCards/{orderId}")
    @ResponseBody
    public CommonResult<String> showCards(@PathVariable("orderId") Long orderId) {
        String cards = portalOrderService.showCards(orderId, memberService.getCurrentMember().getId());
        return CommonResult.success(cards);
    }

    @GetMapping("/canPay/{orderId}")
    @ResponseBody
    public CommonResult<String> canPay(@PathVariable("orderId") Long orderId) {
        String result = "ok";
        OmsOrderDetail detail = portalOrderService.detail(orderId);
        if (detail.getStatus() != 0) {
            result = "该订单不处于可付款状态";
        }
        UmsMember currentMember = memberService.getCurrentMember();
        IdentityResultBO identityResultBO = identityService.identityIdNumber(currentMember.getId());
        if (!identityResultBO.getHasIdentity()) {
            result = "请先实名认证";
        } else {
            String idNo = identityResultBO.getIdNo();
            int ageFromIdCard = getAgeFromIdCard(idNo);
            if (ageFromIdCard < 18) {
                result = "未成年人无法充值";
            }
        }

        return CommonResult.success(result);

    }

    private int getAgeFromIdCard(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            throw new IllegalArgumentException("身份证号格式不正确");
        }

        // 从身份证号中截取出生日期
        String birthDateStr = idCard.substring(6, 14);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate birthDate = LocalDate.parse(birthDateStr, formatter);

        // 计算年龄
        LocalDate currentDate = LocalDate.now();
        return (int) ChronoUnit.YEARS.between(birthDate, currentDate);
    }

    @Operation(summary = "发起退款")
    @PostMapping("/refund")
    @ResponseBody
    public CommonResult<String> refund(@RequestParam("orderId") Long id, @RequestParam("reason") String reason) {
        try {
            String hint = portalOrderService.refund(id, reason);
            return CommonResult.success(hint);
        } catch (Exception e) {
            log.error("refund error", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "根据购物车信息生成确认单信息")
    @RequestMapping(value = "/generateConfirmOrder", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<ConfirmOrderResult> generateConfirmOrder(@RequestBody List<Long> cartIds) {
        ConfirmOrderResult confirmOrderResult = portalOrderService.generateConfirmOrder(cartIds);
        return CommonResult.success(confirmOrderResult);
    }

    @Operation(summary = "根据购物车信息生成订单")
    @RequestMapping(value = "/generateOrder", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult generateOrder(@RequestBody OrderParam orderParam) {
        Map<String, Object> result = portalOrderService.generateOrder(orderParam);
        return CommonResult.success(result, "下单成功");
    }

    @Operation(summary = "根据购物车信息修改属性并生成订单")
    @RequestMapping(value = "/generateOrderWithAttribute", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult generateOrderWithAttribute(@RequestBody OrderParamWithAttribute orderParam) {
        Map<String, Object> result = portalOrderService.generateOrderWithAttribute(orderParam);
        return CommonResult.success(result, "下单成功");
    }


    @Operation(summary = "用户支付成功的回调")
    @RequestMapping(value = "/paySuccess", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult paySuccess(@RequestParam Long orderId, @RequestParam Integer payType) {
        Integer count = portalOrderService.paySuccess(orderId, payType);
        return CommonResult.success(count, "支付成功");
    }

    @Operation(summary = "自动取消超时订单")
    @RequestMapping(value = "/cancelTimeOutOrder", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult cancelTimeOutOrder() {
        portalOrderService.cancelTimeOutOrder();
        return CommonResult.success(null);
    }

    @Operation(summary = "获取订单提示信息")
    @GetMapping(value = "hint/{orderId}")
    @ResponseBody
    public CommonResult<String> hint(@PathVariable("orderId") Long orderId) {
        String hint = portalOrderService.hint(orderId);
        return CommonResult.success(hint);
    }

    @Operation(summary = "按状态分页获取用户订单列表")
    @Parameter(name = "status", description = "订单状态：-1->全部；0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭",
            in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "-1", allowableValues = {"-1", "0", "1", "2", "3", "4"}))
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<OmsOrderDetail>> list(@RequestParam Integer status,
                                                         @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                                         @RequestParam(required = false, defaultValue = "5") Integer pageSize) {
        CommonPage<OmsOrderDetail> orderPage = portalOrderService.list(status, pageNum, pageSize);
        return CommonResult.success(orderPage);
    }

    @Operation(summary = "根据ID获取订单详情")
    @RequestMapping(value = "/detail/{orderId}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<OmsOrderDetail> detail(@PathVariable Long orderId) {
        OmsOrderDetail orderDetail = portalOrderService.detail(orderId);
        return CommonResult.success(orderDetail);
    }

    @Operation(summary = "用户取消订单")
    @RequestMapping(value = "/cancelUserOrder", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult cancelUserOrder(Long orderId) {
        portalOrderService.cancelOrder(orderId);
        return CommonResult.success(null);
    }

    @Operation(summary = "用户确认收货")
    @RequestMapping(value = "/confirmReceiveOrder", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult confirmReceiveOrder(Long orderId) {
        portalOrderService.confirmReceiveOrder(orderId);
        return CommonResult.success(null);
    }

    @Operation(summary = "用户删除订单")
    @RequestMapping(value = "/deleteOrder", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult deleteOrder(Long orderId) {
        portalOrderService.deleteOrder(orderId);
        return CommonResult.success(null);
    }
}
