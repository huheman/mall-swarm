package com.macro.mall.portal.service.impl;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.macro.mall.portal.service.AttributeChecker;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AttributeCheckerImpl implements AttributeChecker {
    @Autowired
    private OkHttpClient httpClient;

    @Override
    public String checkAttribute(Long gameId, JSONArray attributeValue) {
        String isLegal = "ok";
        if (gameId == 57) {
            for (int i = 0; i < attributeValue.size(); i++) {
                JSONObject jsonObject = attributeValue.getJSONObject(i);
                String key = jsonObject.getString("key");
                if (StringUtils.contains(key, "username")) {
                    String userName = jsonObject.getString("value");
                    // 构建请求URL
                    String url = "http://120.24.168.170:8080/midasbuy/getCharac?appid=1450015065&zoneid=1&playerId=" + userName;
                    // 创建GET请求
                    Request request = new Request.Builder()
                            .url(url)
                            .get()
                            .build();
                    Call call = httpClient.newCall(request);
                    try {
                        // 执行请求并获取响应
                        Response response = call.execute();
                        Assert.state(response.isSuccessful(), "responseCode" + response.code());

                        String responseBody = response.body().string();
                        JSONObject json = JSON.parseObject(responseBody);
                        Integer status = json.getInteger("status");
                        if (status != null && status != 0) {
                            isLegal = String.format("【%s】未能通过校验。", fitName(key));
                        }
                    } catch (Exception e) {
                        log.error("查询username是否存在失败了", e);
                    }
                    break;
                }
            }
        }
        return isLegal;
    }

    private String fitName(String item) {
        // 检查字符串中是否存在'-'
        if (item.indexOf('-') > 0) {
            // 如果存在，使用split('-')，返回分割后的第一个部分
            return item.split("-")[0];
        } else {
            // 如果不存在'-'，直接返回原字符串
            return item;
        }
    }
}
