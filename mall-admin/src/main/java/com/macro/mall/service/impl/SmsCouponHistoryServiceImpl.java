package com.macro.mall.service.impl;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.macro.mall.common.api.CommonPage;
import com.macro.mall.dto.SmsCouponHistoryDTO;
import com.macro.mall.mapper.SmsCouponHistoryMapper;
import com.macro.mall.mapper.UmsMemberMapper;
import com.macro.mall.model.SmsCouponHistory;
import com.macro.mall.model.SmsCouponHistoryExample;
import com.macro.mall.model.UmsMember;
import com.macro.mall.model.UmsMemberExample;
import com.macro.mall.service.SmsCouponHistoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 优惠券领取记录管理Service实现类
 * Created by macro on 2018/11/6.
 */
@Service
public class SmsCouponHistoryServiceImpl implements SmsCouponHistoryService {
    @Autowired
    private SmsCouponHistoryMapper historyMapper;
    @Autowired
    private UmsMemberMapper memberMapper;

    @Override
    public CommonPage<SmsCouponHistoryDTO> list(Long couponId, Integer useStatus, String orderSn, Integer pageSize, Integer pageNum) {
        SmsCouponHistoryExample example = new SmsCouponHistoryExample();
        SmsCouponHistoryExample.Criteria criteria = example.createCriteria();
        if (couponId != null) {
            criteria.andCouponIdEqualTo(couponId);
        }
        if (useStatus != null) {
            criteria.andUseStatusEqualTo(useStatus);
        }
        if (!StringUtils.isEmpty(orderSn)) {
            criteria.andOrderSnEqualTo(orderSn);
        }

        Page<SmsCouponHistory> objects = PageHelper.startPage(pageNum, pageSize).doSelectPage(new ISelect() {
            @Override
            public void doSelect() {
                historyMapper.selectByExample(example);
            }
        });
        List<SmsCouponHistory> smsCouponHistories = objects.getResult();
        if (smsCouponHistories == null || smsCouponHistories.isEmpty()) {
            return CommonPage.restPage(new ArrayList<>(), 0L);
        }

        List<Long> memberIds = smsCouponHistories.stream().map(SmsCouponHistory::getMemberId).toList();
        UmsMemberExample umsMemberExample = new UmsMemberExample();
        umsMemberExample.createCriteria().andIdIn(memberIds);
        Map<Long, UmsMember> collect = memberMapper.selectByExample(umsMemberExample).stream().collect(Collectors.toMap(UmsMember::getId, umsMember -> umsMember));

        List<SmsCouponHistoryDTO> list = smsCouponHistories.stream().map(smsCouponHistory -> {
            SmsCouponHistoryDTO smsCouponHistoryDTO = new SmsCouponHistoryDTO();
            BeanUtils.copyProperties(smsCouponHistory, smsCouponHistoryDTO);
            UmsMember umsMember = collect.get(smsCouponHistory.getMemberId());
            if (umsMember != null) {
                smsCouponHistoryDTO.setPhone(umsMember.getPhone());
            }
            return smsCouponHistoryDTO;
        }).toList();
        return CommonPage.restPage(list, objects.getTotal());
    }
}
