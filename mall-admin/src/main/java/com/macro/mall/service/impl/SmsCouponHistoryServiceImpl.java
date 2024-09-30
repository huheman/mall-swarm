package com.macro.mall.service.impl;

import com.github.pagehelper.PageHelper;
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
    public List<SmsCouponHistoryDTO> list(Long couponId, Integer useStatus, String orderSn, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
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
        List<SmsCouponHistory> smsCouponHistories = historyMapper.selectByExample(example);
        if (smsCouponHistories == null || smsCouponHistories.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> memberIds = smsCouponHistories.stream().map(SmsCouponHistory::getMemberId).toList();
        UmsMemberExample umsMemberExample = new UmsMemberExample();
        umsMemberExample.createCriteria().andIdIn(memberIds);
        Map<Long, UmsMember> collect = memberMapper.selectByExample(umsMemberExample).stream().collect(Collectors.toMap(UmsMember::getId, umsMember -> umsMember));

        return smsCouponHistories.stream().map(smsCouponHistory -> {
            SmsCouponHistoryDTO smsCouponHistoryDTO = new SmsCouponHistoryDTO();
            BeanUtils.copyProperties(smsCouponHistory, smsCouponHistoryDTO);
            smsCouponHistoryDTO.setPhone(collect.get(smsCouponHistory.getMemberId()).getPhone());
            return smsCouponHistoryDTO;
        }).toList();
    }
}
