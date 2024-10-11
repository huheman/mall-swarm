package com.macro.mall.service.impl;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.exception.ApiException;
import com.macro.mall.mapper.SmsKolPromoConfigMapper;
import com.macro.mall.model.SmsKolPromoConfig;
import com.macro.mall.model.SmsKolPromoConfigExample;
import com.macro.mall.service.KOLPromoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class KOLPromoServiceImpl implements KOLPromoService {
    @Autowired
    private SmsKolPromoConfigMapper smsKolPromoConfigMapper;

    @Override
    public SmsKolPromoConfig create(String kolName, String kolId, String qrCode, String h5Link) {
        if (StringUtils.isEmpty(kolId) || StringUtils.isEmpty(qrCode) || StringUtils.isEmpty(h5Link)) {
            throw new ApiException("kolId不能为空");
        }
        SmsKolPromoConfigExample smsKolPromoConfigExample = new SmsKolPromoConfigExample();
        smsKolPromoConfigExample.createCriteria().andKolIdEqualTo(kolId);

        List<SmsKolPromoConfig> smsKolPromoConfigs = smsKolPromoConfigMapper.selectByExample(smsKolPromoConfigExample);
        if (!smsKolPromoConfigs.isEmpty()) {
            throw new ApiException("kolId" + kolId + "已经存在了");
        }
        SmsKolPromoConfig smsKolPromoConfig = new SmsKolPromoConfig();
        smsKolPromoConfig.setKolName(kolName);
        smsKolPromoConfig.setKolId(kolId);
        smsKolPromoConfig.setKolQrCode(qrCode);
        smsKolPromoConfig.setKolH5Link(h5Link);
        smsKolPromoConfig.setCreateTime(new Date());
        smsKolPromoConfigMapper.insertSelective(smsKolPromoConfig);
        return smsKolPromoConfig;
    }

    @Override
    public CommonPage<SmsKolPromoConfig> page(int pageNo, int pageSize) {
        Page<SmsKolPromoConfig> objects = PageHelper.startPage(pageNo, pageSize).doSelectPage(new ISelect() {
            @Override
            public void doSelect() {
                SmsKolPromoConfigExample smsKolPromoConfigExample = new SmsKolPromoConfigExample();
                smsKolPromoConfigExample.setOrderByClause("id desc");
                smsKolPromoConfigMapper.selectByExample(smsKolPromoConfigExample);
            }
        });
        return CommonPage.restPage(objects.getResult(), objects.getTotal());
    }


}
