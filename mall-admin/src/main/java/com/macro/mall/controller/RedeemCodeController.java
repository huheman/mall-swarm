package com.macro.mall.controller;

import cn.hutool.json.JSONObject;
import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.dto.FullRedeemCodeRecordDTO;
import com.macro.mall.dto.RedeemCodeGenerateVO;
import com.macro.mall.dto.RedeemSearchVO;
import com.macro.mall.model.RedeemCodeRecord;
import com.macro.mall.model.SmsKolPromoConfig;
import com.macro.mall.service.RedeemService;
import com.macro.mall.service.impl.KOLPromoServiceImpl;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    @GetMapping("download")
    @SneakyThrows
    public void download(HttpServletResponse response, @RequestParam("query") String query) {
        JSONObject jsonObject = new JSONObject(query);
        RedeemSearchVO bean = jsonObject.toBean(RedeemSearchVO.class);
        // 设置响应头
        response.setContentType("text/csv;charset=UTF-8");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
        String fileName = dateFormat.format(new Date()) + "redeem_code_detail.csv";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        ServletOutputStream outputStream = response.getOutputStream();
        // 写入 BOM 到文件头，确保 Windows Excel 正确识别 UTF-8 编码
        outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        redeemService.download(bean, outputStreamWriter);
        outputStreamWriter.close();
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
