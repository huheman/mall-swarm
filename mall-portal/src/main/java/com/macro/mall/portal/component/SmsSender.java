package com.macro.mall.portal.component;

import com.alibaba.fastjson.JSONObject;
import com.tencentcloudapi.common.AbstractModel;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.tencentcloudapi.common.AbstractModel;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class SmsSender {
    @Value("${sms.app.id}")
    private String appId;
    @Value("${sms.app.sign.name}")
    private String signName;
    @Value("${sms.authCode.template}")
    private String authCodeTemplate;
    @Autowired
    private SmsClient smsClient;

    public Integer sendAuthCode(String receivePhone, String authCode) {
        return send(Collections.singletonList(receivePhone),
                Collections.singletonList(authCode), authCodeTemplate);
    }

    @SneakyThrows
    public Integer send(Collection<String> receivePhone, List<String> param, String templateId) {
        if (CollectionUtils.isEmpty(receivePhone)) {
            return 0;
        }
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            SendSmsRequest req = new SendSmsRequest();
            String[] params = param.toArray(new String[param.size()]);
            req.setTemplateParamSet(params);
            req.setSignName(signName);
            req.setSmsSdkAppId(appId);
            req.setTemplateId(templateId);
            String[] phoneNums = receivePhone.stream()
                    .filter(s -> s != null && !s.isEmpty())
                    .map(s -> s.startsWith("+86") ? s : "+86" + s).toArray(String[]::new);
            req.setPhoneNumberSet(phoneNums);
            // 返回的resp是一个SendSmsResponse的实例，与请求对象对应
            SendSmsResponse resp = smsClient.SendSms(req);
            // 输出json格式的字符串回包
            log.info(AbstractModel.toJsonString(resp));
            return 1;
        } catch (Exception e) {
            log.error("发送短信失败", e);
            return 0;
        }
    }

    private String buildJsonRequestBody(Collection<String> phones, Collection<String> params, String templateId) {
        JSONObject param = new JSONObject();
        phones = phones.stream()
                .filter(s -> s != null && !s.isEmpty())
                .map(s -> s.startsWith("+86") ? s : "+86" + s).toList();
        param.put("PhoneNumberSet", phones);
        param.put("SmsSdkAppId", appId);
        param.put("SignName", signName);
        param.put("Action", "sendSms");
        param.put("Version", "2021-01-11");
        param.put("Region", "ap-guangzhou");
        param.put("TemplateId", templateId);
        param.put("TemplateParamSet", params);
        return param.toJSONString();
    }
}
