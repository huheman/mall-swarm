package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.portal.config.AlipayConfig;
import com.macro.mall.portal.domain.AliPayParam;
import com.macro.mall.portal.domain.OmsOrderDetail;
import com.macro.mall.portal.service.AlipayService;
import com.macro.mall.portal.service.DirectChargeService;
import com.macro.mall.portal.service.OmsPortalOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @auther macrozheng
 * @description 支付宝支付Controller
 * @date 2023/9/8
 * @github https://github.com/macrozheng
 */
@Controller
@Tag(name = "AlipayController", description = "支付宝支付相关接口")
@RequestMapping("/alipay")
@Slf4j
public class AlipayController {

    @Autowired
    private AlipayConfig alipayConfig;
    @Autowired
    private AlipayService alipayService;
    @Autowired
    private OmsPortalOrderService omsPortalOrderService;
    @Autowired
    private DirectChargeService directChargeService;

    @Operation(summary = "支付宝手机网站支付")
    @RequestMapping(value = "/webPay", method = RequestMethod.GET)
    public void webPay(AliPayParam aliPayParam, HttpServletResponse response) throws Exception {
        response.setContentType("text/html;charset=" + alipayConfig.getCharset());
        OmsOrderDetail detail = omsPortalOrderService.detail(aliPayParam.getOrderId());
        response.getWriter().write(alipayService.webPay(aliPayParam, detail));
        response.getWriter().flush();
        response.getWriter().close();
    }


    @Operation(summary = "支付宝异步回调", description = "必须为POST请求，执行成功返回success，执行失败返回failure")
    @RequestMapping(value = "/notify", method = RequestMethod.POST)
    public String notify(HttpServletRequest request) throws Exception {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            params.put(name, request.getParameter(name));
        }
        try {
            String orderSN = alipayService.notifyPay(params);
            omsPortalOrderService.paySuccessByOrderSn(orderSN, 1);
            directChargeService.directCharge(orderSN);
        } catch (Exception e) {
            log.error("支付宝支付回调失败", e);
            throw e;
        }
        return "success";
    }

    @Operation(summary = "支付宝统一收单线下交易查询", description = "订单支付成功返回交易状态：TRADE_SUCCESS")
    @RequestMapping(value = "/query", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<String> query(String outTradeNo, String tradeNo) throws Exception {
        String tradStatus = alipayService.query(outTradeNo, tradeNo);

        if ("TRADE_SUCCESS".equals(tradStatus)) {
            omsPortalOrderService.paySuccessByOrderSn(outTradeNo, 1);
        }
        return CommonResult.success(tradStatus);
    }
}
