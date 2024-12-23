package com.macro.mall.controller;

import com.macro.mall.dto.RedeemCodeGenerateVO;
import com.macro.mall.service.RedeemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("redeemCode")
public class RedeemCodeController {
    @Autowired
    private RedeemService redeemService;


    @PostMapping("generate")
    public String generate(@RequestBody RedeemCodeGenerateVO redeemCodeGenerateVO) {
        redeemService.generateRedeemCode(redeemCodeGenerateVO.getGenerateCount(), redeemCodeGenerateVO.getSkuId(), redeemCodeGenerateVO.getKolId());
        return "ok";
    }
}
