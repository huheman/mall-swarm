package com.macro.mall.portal.service;

import com.alibaba.fastjson.JSONArray;

public interface AttributeChecker {
    String checkAttribute(Long gameId, JSONArray attributeValue);
}
