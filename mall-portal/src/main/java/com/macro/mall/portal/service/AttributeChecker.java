package com.macro.mall.portal.service;

import com.alibaba.fastjson.JSONObject;

public interface AttributeChecker {
    boolean checkAttribute(Long gameId, JSONObject attributeValue);
}
