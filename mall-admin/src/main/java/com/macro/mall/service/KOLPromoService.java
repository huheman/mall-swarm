package com.macro.mall.service;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.model.SmsKolPromoConfig;

public interface KOLPromoService {
    SmsKolPromoConfig create(String kolName, String kolId,String qrCode,String h5Link);

    CommonPage<SmsKolPromoConfig> page(int pageNo, int pageSize);
}
