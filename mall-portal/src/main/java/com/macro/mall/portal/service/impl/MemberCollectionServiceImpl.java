package com.macro.mall.portal.service.impl;

import com.macro.mall.portal.domain.MemberProductCollection;
import com.macro.mall.portal.service.MemberCollectionService;
import com.macro.mall.portal.service.UmsMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 * 会员收藏Service实现类
 * Created by macro on 2018/8/2.
 */
@Service
public class MemberCollectionServiceImpl implements MemberCollectionService {
    @Autowired
    private UmsMemberService memberService;

    @Override
    public int add(MemberProductCollection productCollection) {
        return 1;
    }

    @Override
    public int delete(Long productId) {
        return 1;
    }

    @Override
    public Page<MemberProductCollection> list(Integer pageNum, Integer pageSize) {
        return Page.empty();
    }

    @Override
    public MemberProductCollection detail(Long productId) {
        return null;
    }

    @Override
    public void clear() {
    }
}
