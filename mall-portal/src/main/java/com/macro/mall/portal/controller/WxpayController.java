package com.macro.mall.portal.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.mapper.OmsOrderMapper;
import com.macro.mall.portal.domain.OmsOrderDetail;
import com.macro.mall.portal.service.DirectChargeService;
import com.macro.mall.portal.service.OmsPortalOrderService;
import com.macro.mall.portal.service.UmsMemberService;
import com.macro.mall.portal.service.WxPayService;
import com.macro.mall.portal.service.bo.OpenIdBO;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import com.wechat.pay.java.service.refund.model.Status;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "WxpayController", description = "微信支付相关接口")
@RequestMapping("/wxpay")
@Slf4j
public class WxpayController {
    @Autowired
    private WxPayService wxPayService;
    @Value("${sa-token.token-prefix}")
    private String tokenHead;

    @Autowired
    private UmsMemberService umsMemberService;
    @Autowired
    private OmsPortalOrderService omsPortalOrderService;
    @Autowired
    private DirectChargeService directChargeService;


    /*微信下单分为两步。第一步获取replay_id，小程序通过repay_id调起小程序支付模块进行付款*/
    @Operation(summary = "使用微信付款")
    @GetMapping("/createOrder")
    public CommonResult<PrepayWithRequestPaymentResponse> createOrder(String openId, Long orderId) {
        OmsOrderDetail detail = omsPortalOrderService.detail(orderId);
        PrepayWithRequestPaymentResponse response = wxPayService.createOrder(openId, detail);

        return CommonResult.success(response);
    }


    @Operation(summary = "退款结果回调")
    @PostMapping("refundNotify")
    @ResponseBody
    @SneakyThrows
    public CommonResult<Boolean> refundNotify(HttpServletRequest request) {
        String signature = request.getHeader("Wechatpay-Signature");
        String serial = request.getHeader("Wechatpay-Serial");
        String nonc = request.getHeader("Wechatpay-Nonce");
        String wechatTimestamp = request.getHeader("Wechatpay-Timestamp");
        String signType = request.getHeader("Wechatpay-Signature-Type");
        byte[] bytes = request.getInputStream().readAllBytes();
        String requestBody = new String(bytes, StandardCharsets.UTF_8);
        RefundNotification refundNotification = wxPayService.notifyRefund(requestBody, signature, serial, nonc, wechatTimestamp, signType);
        if (refundNotification.getRefundStatus() == Status.SUCCESS) {
            log.info("微信退款成功");
            omsPortalOrderService.refundSuccess(refundNotification.getOutTradeNo());
        } else {
            log.error("收到退款失败信息:{}", refundNotification);
        }
        return CommonResult.success(true);
    }


    @Operation(summary = "获取微信openId")
    @GetMapping("getOpenId")
    @SneakyThrows
    public CommonResult<OpenIdBO> getOpenId(@RequestParam String code) {
        OpenIdBO openId = wxPayService.getOpenId(code);
        // 拼接微信接口的 URL
        return CommonResult.success(openId);
    }

    @GetMapping("/ship")
    public CommonResult<Boolean> ship(Long orderId) {
        OmsOrderDetail detail = omsPortalOrderService.detail(orderId);
        wxPayService.uploadShipping(detail);
        return CommonResult.success(true);
    }

    @SneakyThrows
    @PostMapping("notify")
    @ResponseBody
    public CommonResult<String> notify(HttpServletRequest request) {
        String signature = request.getHeader("Wechatpay-Signature");
        String serial = request.getHeader("Wechatpay-Serial");
        String nonc = request.getHeader("Wechatpay-Nonce");
        String wechatTimestamp = request.getHeader("Wechatpay-Timestamp");
        String signType = request.getHeader("Wechatpay-Signature-Type");
        byte[] bytes = request.getInputStream().readAllBytes();
        String requestBody = new String(bytes, StandardCharsets.UTF_8);
        Transaction transaction = wxPayService.notifyPay(requestBody, signature, serial, nonc, wechatTimestamp, signType);
        if (transaction.getTradeState()== Transaction.TradeStateEnum.SUCCESS) {
            String outTradeNo = transaction.getOutTradeNo();
            omsPortalOrderService.updateMoreInfo(outTradeNo, "openId", transaction.getPayer().getOpenid());
            omsPortalOrderService.paySuccessByOrderSn(outTradeNo, 2);
            directChargeService.directCharge(outTradeNo);
        }
        return CommonResult.success(serial);
    }

    @GetMapping("loginByWxPhoneCode")
    @ResponseBody
    public CommonResult getPhoneNum(String code) {
        String phone = wxPayService.getPhone(code);
        SaTokenInfo saTokenInfo = umsMemberService.loginByPhone(phone);
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", saTokenInfo.getTokenValue());
        tokenMap.put("tokenHead", tokenHead + " ");
        return CommonResult.success(tokenMap);
    }


}
