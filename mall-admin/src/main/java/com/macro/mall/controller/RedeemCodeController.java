package com.macro.mall.controller;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.dto.FullRedeemCodeRecordDTO;
import com.macro.mall.dto.RedeemCodeGenerateVO;
import com.macro.mall.dto.RedeemSearchVO;
import com.macro.mall.model.RedeemCodeRecord;
import com.macro.mall.model.SmsKolPromoConfig;
import com.macro.mall.service.RedeemService;
import com.macro.mall.service.impl.KOLPromoServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("redeemCode")
public class RedeemCodeController {
    @Autowired
    private RedeemService redeemService;
    @Autowired
    private KOLPromoServiceImpl kolPromoServiceImpl;


    @PostMapping("generate")
    public CommonResult<String> generate(@RequestBody RedeemCodeGenerateVO redeemCodeGenerateVO) {
        try {
            redeemService.generateRedeemCode(redeemCodeGenerateVO.getGenerateCount(), redeemCodeGenerateVO.getSkuId(), redeemCodeGenerateVO.getKolId());
            return CommonResult.success("ok");
        }catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }

    }

    @PostMapping("list")
    public CommonResult<CommonPage<FullRedeemCodeRecordDTO>> list(@RequestBody RedeemSearchVO redeemSearchVO) {
        CommonPage<RedeemCodeRecord> page = redeemService.page(redeemSearchVO);
        List<FullRedeemCodeRecordDTO> fullList = redeemService.transfer(page.getList());
        return CommonResult.success(CommonPage.restPage(fullList, page.getTotal()));
    }

    @GetMapping("allKol")
    public CommonResult<List<SmsKolPromoConfig>> allKol() {
        return CommonResult.success(kolPromoServiceImpl.allKol());
    }
}
