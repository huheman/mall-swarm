package com.macro.mall.portal.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.macro.mall.mapper.OmsOrderMapper;
import com.macro.mall.model.OmsOrder;
import com.macro.mall.portal.config.AlipayConfig;
import com.macro.mall.portal.domain.AliPayParam;
import com.macro.mall.portal.service.AlipayService;
import com.macro.mall.portal.service.DirectChargeService;
import com.macro.mall.portal.service.OmsPortalOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @auther macrozheng
 * @description 支付宝支付Service实现类
 * @date 2023/9/8
 * @github https://github.com/macrozheng
 */
@Slf4j
@Service
public class AlipayServiceImpl implements AlipayService {
    @Autowired
    private AlipayConfig alipayConfig;
    @Autowired
    private AlipayClient alipayClient;

    @Override
    public String notifyPay(Map<String, String> params) throws Exception {
        boolean signVerified;
        try {
            //调用SDK验证签名
            signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(), alipayConfig.getCharset(), alipayConfig.getSignType());
        } catch (AlipayApiException e) {
            log.error("支付回调签名校验异常！", e);
            throw e;
        }
        Assert.state(signVerified, "支付宝回调签名验签失败");
        Assert.state("TRADE_SUCCESS".equals(params.get("trade_status")), "订单未能支付成功");
        return params.get("out_trade_no");
    }

    @Override
    public String query(String outTradeNo, String tradeNo) throws Exception {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        //******必传参数******
        JSONObject bizContent = new JSONObject();
        //设置查询参数，out_trade_no和trade_no至少传一个
        if (StrUtil.isNotEmpty(outTradeNo)) {
            bizContent.put("out_trade_no", outTradeNo);
        }
        if (StrUtil.isNotEmpty(tradeNo)) {
            bizContent.put("trade_no", tradeNo);
        }
        //交易结算信息: trade_settle_info
        String[] queryOptions = {"trade_settle_info"};
        bizContent.put("query_options", queryOptions);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            log.error("查询支付宝账单异常！", e);
            throw e;
        }
        Assert.state(response.isSuccess(), "查询支付宝账单失败！" + response.getMsg());
        return response.getTradeStatus();
    }

    /*https://opendocs.alipay.com/open/02ivbs?scene=21*/
    @Override
    public String webPay(AliPayParam aliPayParam,OmsOrder omsOrder) {
        log.info("webPay请求{}", JSON.toJSON(aliPayParam));
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        if (StrUtil.isNotEmpty(alipayConfig.getNotifyUrl())) {
            //异步接收地址，公网可访问
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
        }
        if (StrUtil.isNotEmpty(alipayConfig.getReturnUrl())) {
            //同步跳转地址
            request.setReturnUrl(alipayConfig.getReturnUrl());
        }
        //******必传参数******
        JSONObject bizContent = new JSONObject();
        //商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", omsOrder.getOrderSn());
        //支付金额，最小值0.01元
        bizContent.put("total_amount", omsOrder.getPayAmount());
        //订单标题，不可使用特殊符号
        bizContent.put("subject", aliPayParam.getSubject());
        //手机网站支付默认传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "QUICK_WAP_WAY");
        request.setBizContent(bizContent.toString());
        String formHtml = null;
        try {
            formHtml = alipayClient.pageExecute(request).getBody();
            log.info("webPay成功{}", formHtml);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return formHtml;
    }


    /*https://opendocs.alipay.com/open/4b7cc5db_alipay.trade.refund?scene=common&pathHash=d98b006d*/
    public void refund(OmsOrder order) throws Exception {
        // 构造请求参数以调用接口
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();

        // 设置商户订单号
        model.setOutTradeNo(order.getOrderSn());


        // 设置退款金额
        model.setRefundAmount(order.getPayAmount().stripTrailingZeros().toPlainString());

        // 设置退款原因说明
        model.setRefundReason("正常退款");


        request.setBizModel(model);
        // 第三方代调用模式下请设置app_auth_token
        // request.putOtherTextParam("app_auth_token", "<-- 请填写应用授权令牌 -->");

        AlipayTradeRefundResponse response = alipayClient.execute(request);
        log.info(response.getBody());
        Assert.state(response.isSuccess() &&response.getCode().equals("10000") , "支付宝退款失败" + response.getMsg());
    }
}
