package com.macro.mall.portal.service.bo;

import lombok.Data;

@Data
public class IdentityResultBO {
    // 真实姓名
    private String realName;
    // 身份证好
    private String idNo;
    // 是否已经验证
    private Boolean hasIdentity;
}
