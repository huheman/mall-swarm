package com.macro.mall.portal.service.impl;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
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
    public boolean checkAttribute(Long gameId, JSONObject jsonObject) {
        boolean isLegal = true;
        if (gameId == 57) {
            String key = jsonObject.getString("key");
            String value = jsonObject.getString("value");
            if (StringUtils.contains(key, "username") && StringUtils.isNoneEmpty(value)) {
                String userName = value;
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
                        isLegal = false;
                    }
                } catch (Exception e) {
                    log.error("查询username是否存在失败了", e);
                }
            }
        }
        return isLegal;
    }
}
