package com.macro.mall.portal.controller.vo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class AttributeBO {
    private Long gameId;
    private JSONArray attributeList;
    private JSONObject currentAttribute;
}
