package com.macro.mall.controller;

import cn.hutool.json.JSONObject;
import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.dto.*;
import com.macro.mall.service.OmsOrderService;
import com.macro.mall.service.PortalOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 订单管理Controller
 * Created by macro on 2018/10/11.
 */
@Controller
@Tag(name = "OmsOrderController", description = "订单管理")
@Slf4j
@RequestMapping("/order")
public class OmsOrderController {
    @Autowired
    private OmsOrderService orderService;
    @Autowired
    private PortalOrderService portalOrderService;

    @Operation(summary = "查询订单")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<OmsOrderWithDirectCharge>> list(OmsOrderQueryParam queryParam,
                                                                   @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                                   @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        CommonPage<OmsOrderWithDirectCharge> orderList = orderService.list(queryParam, pageSize, pageNum);
        return CommonResult.success(orderList);
    }

    @GetMapping("download")
    @SneakyThrows
    public void download(HttpServletResponse response, @RequestParam("query") String query) {
        JSONObject jsonObject = new JSONObject(query);
        OmsOrderQueryParam bean = jsonObject.toBean(OmsOrderQueryParam.class);
        // 设置响应头
        response.setContentType("text/csv;charset=UTF-8");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
        String fileName = dateFormat.format(new Date()) + "order_detail.csv";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        ServletOutputStream outputStream = response.getOutputStream();
        // 写入 BOM 到文件头，确保 Windows Excel 正确识别 UTF-8 编码
        outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        orderService.download(bean, outputStreamWriter);
        outputStreamWriter.close();
    }

    @Operation(summary = "发起订单退款")
    @PostMapping("/refund/{orderId}")
    @ResponseBody
    public CommonResult<String> refund(@PathVariable("orderId") Long orderId, @RequestParam("note") String reason) {
        return portalOrderService.refund(orderId, reason);
    }

    @Operation(summary = "批量发货")
    @RequestMapping(value = "/update/delivery", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult delivery(@RequestBody List<OmsOrderDeliveryParam> deliveryParamList) {
        int count = orderService.delivery(deliveryParamList);
        // 完成微信的发货
        for (OmsOrderDeliveryParam omsOrderDeliveryParam : deliveryParamList) {
            try {
                portalOrderService.ship(omsOrderDeliveryParam.getOrderId());
                portalOrderService.confirmReceiveOrder(omsOrderDeliveryParam.getOrderId());
            } catch (Exception e) {
                log.error(omsOrderDeliveryParam + "发货失败", e);
            }
        }
        return CommonResult.success(count);
    }

    @Operation(summary = "批量关闭订单")
    @RequestMapping(value = "/update/close", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult close(@RequestParam("ids") List<Long> ids, @RequestParam String note) {
        for (Long id : ids) {
            try {
                portalOrderService.closeOrder(id, "后台管理员", note);
            } catch (Exception e) {
                log.error("关闭订单失败", e);
            }
        }
        return CommonResult.success(true);
    }

    @Operation(summary = "批量删除订单")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult delete(@RequestParam("ids") List<Long> ids) {
        int count = orderService.delete(ids);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "获取订单详情:订单信息、商品信息、操作记录")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<OmsOrderDetail> detail(@PathVariable Long id) {
        OmsOrderDetail orderDetailResult = orderService.detail(id);
        return CommonResult.success(orderDetailResult);
    }

    @Operation(summary = "修改收货人信息")
    @RequestMapping(value = "/update/receiverInfo", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateReceiverInfo(@RequestBody OmsReceiverInfoParam receiverInfoParam) {
        int count = orderService.updateReceiverInfo(receiverInfoParam);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "修改订单费用信息")
    @RequestMapping(value = "/update/moneyInfo", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateReceiverInfo(@RequestBody OmsMoneyInfoParam moneyInfoParam) {
        int count = orderService.updateMoneyInfo(moneyInfoParam);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "备注订单")
    @RequestMapping(value = "/update/note", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateNote(@RequestParam("id") Long id,
                                   @RequestParam("note") String note,
                                   @RequestParam("status") Integer status) {
        int count = orderService.updateNote(id, note, status);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}
