package com.macro.mall.portal.service;

import com.macro.mall.portal.service.bo.IdentityResultBO;

public interface IdentityService {
    // 校验后落库
    Boolean identity(Long memberId,String realName, String idNo);


    IdentityResultBO identityIdNumber(Long memberId);

}
