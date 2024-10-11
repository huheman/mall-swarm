package com.macro.mall.service;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.dto.KOLInfoDTO;
import com.macro.mall.model.SmsKolPromoConfig;

import java.util.Date;

public interface KOLPromoService {
    SmsKolPromoConfig create(String kolName, String kolId,String qrCode,String h5Link);

    CommonPage<KOLInfoDTO> page(Integer pageNo, Integer pageSize, String kolName, String kolId, Date startTime, Date endTime);
}
