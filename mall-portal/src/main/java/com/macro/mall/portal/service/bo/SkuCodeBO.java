package com.macro.mall.portal.service.bo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Data
public class SkuCodeBO {
    private String skuCode;
    private Long userId;
    private String attrInfo;

    public Map<String, String> getAttrMap() {
        if (StringUtils.isEmpty(attrInfo)) {
            return Map.of();
        }
        JSONArray jsonArray = JSON.parseArray(attrInfo);
        Map<String, String> attrMap = new HashMap<>();
        jsonArray.forEach(item -> {
            JSONObject jsonObject = (JSONObject) item;
            attrMap.put(jsonObject.getString("key"), jsonObject.getString("value"));
        });
        return attrMap;
    }

}
