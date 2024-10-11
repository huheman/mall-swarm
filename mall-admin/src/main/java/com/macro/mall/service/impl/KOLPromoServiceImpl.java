package com.macro.mall.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.exception.ApiException;
import com.macro.mall.dao.OmsOrderDao;
import com.macro.mall.dto.KOLInfoDTO;
import com.macro.mall.dto.OrderInfoDTO;
import com.macro.mall.mapper.SmsKolPromoConfigMapper;
import com.macro.mall.model.SmsKolPromoConfig;
import com.macro.mall.model.SmsKolPromoConfigExample;
import com.macro.mall.service.KOLPromoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KOLPromoServiceImpl implements KOLPromoService {
    @Autowired
    private SmsKolPromoConfigMapper smsKolPromoConfigMapper;
    @Autowired
    private OmsOrderDao omsOrderDao;

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
    public CommonPage<KOLInfoDTO> page(Integer pageNo, Integer pageSize, String kolName, String kolId, Date startTime, Date endTime) {
        if (pageNo == null || pageSize == null || pageNo < 1 || pageSize < 1 || startTime == null || endTime == null) {
            throw new ApiException("查询入参不对");
        }
        Page<SmsKolPromoConfig> objects = PageHelper.startPage(pageNo, pageSize).doSelectPage(() -> {
            SmsKolPromoConfigExample smsKolPromoConfigExample = new SmsKolPromoConfigExample();
            SmsKolPromoConfigExample.Criteria criteria = smsKolPromoConfigExample.createCriteria();
            if (StringUtils.isNoneEmpty(kolId)) {
                criteria.andKolIdEqualTo(kolId);
            }
            if (StringUtils.isNoneEmpty(kolName)) {
                criteria.andKolNameLike("%" + kolName + "%");
            }
            smsKolPromoConfigExample.setOrderByClause("id desc");
            smsKolPromoConfigMapper.selectByExample(smsKolPromoConfigExample);
        });
        if (objects.getResult().isEmpty()) {
            return CommonPage.restPage(new ArrayList<>(), 0L);
        }
        List<String> kolIds = objects.getResult().stream().map(SmsKolPromoConfig::getKolId).toList();
        List<OrderInfoDTO> orderInfoDTOS = omsOrderDao.getCount(kolIds, startTime, endTime);
        Map<String, OrderInfoDTO> collect = orderInfoDTOS.stream().collect(Collectors.toMap(OrderInfoDTO::getKolId, orderInfoDTO -> orderInfoDTO));
        List<KOLInfoDTO> list = objects.getResult().stream().map(smsKolPromoConfig -> {
            OrderInfoDTO orderInfoDTO = collect.get(smsKolPromoConfig.getKolId());
            KOLInfoDTO kolInfoDTO = new KOLInfoDTO();
            BeanUtils.copyProperties(smsKolPromoConfig, kolInfoDTO);
            if (orderInfoDTO != null) {
                BeanUtils.copyProperties(orderInfoDTO, kolInfoDTO);

            }
            return kolInfoDTO;
        }).toList();
        return CommonPage.restPage(list, objects.getTotal());
    }


}
