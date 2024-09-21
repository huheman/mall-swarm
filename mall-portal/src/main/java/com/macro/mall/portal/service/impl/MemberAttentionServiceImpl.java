package com.macro.mall.portal.service.impl;

import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.domain.MemberBrandAttention;
import com.macro.mall.portal.service.MemberAttentionService;
import com.macro.mall.portal.service.UmsMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 会员关注Service实现类
 * Created by macro on 2018/8/2.
 */
@Service
public class MemberAttentionServiceImpl implements MemberAttentionService {
    @Autowired
    private UmsMemberService memberService;

    @Override
    public int add(MemberBrandAttention memberBrandAttention) {
        int count = 0;
        return count;
    }

    @Override
    public int delete(Long brandId) {
        return 0;
    }

    @Override
    public Page<MemberBrandAttention> list(Integer pageNum, Integer pageSize) {
        return Page.empty();
    }

    @Override
    public MemberBrandAttention detail(Long brandId) {
        return null;
    }

    @Override
    public void clear() {
    }
}
