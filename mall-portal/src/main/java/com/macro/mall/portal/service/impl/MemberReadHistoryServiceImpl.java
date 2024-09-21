package com.macro.mall.portal.service.impl;

import com.macro.mall.portal.domain.MemberReadHistory;
import com.macro.mall.portal.service.MemberReadHistoryService;
import com.macro.mall.portal.service.UmsMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会员浏览记录管理Service实现类
 * Created by macro on 2018/8/3.
 */
@Service
public class MemberReadHistoryServiceImpl implements MemberReadHistoryService {
    @Autowired
    private UmsMemberService memberService;

    @Override
    public int create(MemberReadHistory memberReadHistory) {
        return 1;
    }

    @Override
    public int delete(List<String> ids) {
        return ids.size();
    }

    @Override
    public Page<MemberReadHistory> list(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        return Page.empty();
    }

    @Override
    public void clear() {
    }
}
