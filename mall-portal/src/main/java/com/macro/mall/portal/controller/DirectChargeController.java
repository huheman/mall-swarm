package com.macro.mall.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.portal.service.DirectChargeService;
import com.macro.mall.portal.service.WYTDChargeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/directCharge")
@Slf4j
public class DirectChargeController {
    @Autowired
    private DirectChargeService directChargeService;
    @Autowired
    private WYTDChargeService wytdChargeService;

    @GetMapping("/retry")
    public CommonResult<String> retry(String orderSN) throws Exception {
        directChargeService.directCharge(orderSN);
        return CommonResult.success("ok");
    }


    @PostMapping("/notify")
    public String notify(@RequestBody JSONObject body) throws Exception {
        directChargeService.chargeSuccess(body);
        return "ok";
    }
}
