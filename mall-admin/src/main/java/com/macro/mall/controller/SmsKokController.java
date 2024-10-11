package com.macro.mall.controller;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.common.exception.ApiException;
import com.macro.mall.dto.KOLCreateDTO;
import com.macro.mall.dto.KOLInfoDTO;
import com.macro.mall.dto.KOLSearchDTO;
import com.macro.mall.model.SmsKolPromoConfig;
import com.macro.mall.service.KOLPromoService;
import com.macro.mall.service.PortalOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;

@RequestMapping("/kol")
@RestController
public class SmsKokController {
    private static final Logger log = LoggerFactory.getLogger(SmsKokController.class);
    @Autowired
    private PortalOrderService portalOrderService;
    @Autowired
    private KOLPromoService kolPromoService;
    @Value("${app.homeUrl}")
    private String homeUrl;


    @PostMapping("/create")
    public CommonResult<SmsKolPromoConfig> create(@RequestBody KOLCreateDTO kolCreateDTO) {
        try {
            CommonResult<String> qrcode = portalOrderService.qrcode(kolCreateDTO.getKolId());
            String htmlUrl = homeUrl + "?scene=" + URLEncoder.encode("kol_id=" + kolCreateDTO.getKolId());
            SmsKolPromoConfig smsKolPromoConfig = kolPromoService.create(kolCreateDTO.getKolName(), kolCreateDTO.getKolId(), qrcode.getData(), htmlUrl);
            return CommonResult.success(smsKolPromoConfig);
        } catch (Exception e) {
            log.error("创建kol失败", e);
            throw new ApiException(e.getMessage());
        }
    }

    @PostMapping("list")
    public CommonResult<CommonPage<KOLInfoDTO>> list(@RequestBody KOLSearchDTO kolSearchDTO) {
        CommonPage<KOLInfoDTO> page = kolPromoService.page(kolSearchDTO.getPageNum(), kolSearchDTO.getPageSize(), kolSearchDTO.getKolName(), kolSearchDTO.getKolId(), kolSearchDTO.getStartTime(), kolSearchDTO.getEndTime());
        return CommonResult.success(page);
    }
}
