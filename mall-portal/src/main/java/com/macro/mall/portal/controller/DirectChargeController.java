package com.macro.mall.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.portal.service.DirectChargeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/directCharge")
@Slf4j
public class DirectChargeController {
    @Autowired
    private DirectChargeService directChargeService;

    @GetMapping("/retry")
    public CommonResult<String> test(String orderSN) {
        directChargeService.directCharge(orderSN);
        return CommonResult.success("ok");
    }

    @PostMapping("/notify")
    public JSONObject notify(HttpServletRequest request, @RequestBody JSONObject body) {
        String signer = request.getHeader("X-Signature");
        String timestampStr = request.getHeader("X-Timestamp");
        try {
            directChargeService.chargeSuccess(signer,timestampStr,body);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", "SUCCESS");
            return jsonObject;
        }catch (Exception e){
            log.error("直充通知失败了",e);
            throw e;
        }
    }
}
