package com.macro.mall.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.portal.controller.vo.YZJBody;
import com.macro.mall.portal.service.DirectChargeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/directCharge")
@Slf4j
public class DirectChargeController {
    @Autowired
    private DirectChargeService directChargeService;

    @GetMapping("/retry")
    public CommonResult<String> retry(String orderSN) throws Exception {
        directChargeService.directCharge(orderSN);
        return CommonResult.success("ok");
    }


    @PostMapping("/notify")
    public String notify(@RequestParam Map<String, Object> params) throws Exception {
        directChargeService.chargeSuccess(new JSONObject(params));
        return "OK";
    }

    @PostMapping("/yzjNotify")
    public CommonResult<String> yzjNotify(@RequestBody YZJBody params) throws Exception {
        directChargeService.yzjChargeSuccess(params.getData());
        return CommonResult.success("ok");
    }

    @GetMapping("chargeStatus")
    public String chargeStatus(String orderSN) {
        String result = directChargeService.chargeStatus(orderSN);
        return result;
    }
}
