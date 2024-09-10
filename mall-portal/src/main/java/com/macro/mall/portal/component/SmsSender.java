package com.macro.mall.portal.component;

import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class SmsSender {
    @Autowired
    private OkHttpClient client;
    @Value("${sms.app.id}")
    private String appId;
    @Value("${sms.app.sign.name}")
    private String signName;
    @Value("${sms.authCode.template}")
    private String authCodeTemplate;

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
//            /* 实例化一个请求对象，根据调用的接口和实际情况，可以进一步设置请求参数
//             * 你可以直接查询SDK源码确定接口有哪些属性可以设置
//             * 属性可能是基本类型，也可能引用了另一个数据结构
//             * 推荐使用IDE进行开发，可以方便的跳转查阅各个接口和数据结构的文档说明 */
//            SendSmsRequest req = new SendSmsRequest();
//            /* 填充请求参数,这里request对象的成员变量即对应接口的入参
//             * 你可以通过官网接口文档或跳转到request对象的定义处查看请求参数的定义
//             * 基本类型的设置:
//             * 帮助链接：
//             * 短信控制台: https://console.cloud.tencent.com/smsv2
//             * sms helper: https://cloud.tencent.com/document/product/382/3773 */
//            /* 短信应用ID: 短信SdkAppId在 [短信控制台] 添加应用后生成的实际SdkAppId，示例如1400006666 */
//            req.setSmsSdkAppId(appId);
//            /* 短信签名内容: 使用 UTF-8 编码，必须填写已审核通过的签名，签名信息可登录 [短信控制台] 查看 */
//            req.setSignName(signName);
//            /* 模板 ID: 必须填写已审核通过的模板 ID。模板ID可登录 [短信控制台] 查看 */
//            /* simple类型的短信id就是995102*/
//            req.setTemplateId(templateId);
//            /* 下发手机号码，采用 E.164 标准，+[国家或地区码][手机号]
//             * 示例如：+8613711112222， 其中前面有一个+号 ，86为国家码，13711112222为手机号，最多不要超过200个手机号 */
//            String[] phoneNums = receivePhone.stream()
//                    .filter(s -> s != null && !s.isEmpty())
//                    .map(s -> s.startsWith("+86") ? s : "+86" + s).toArray(String[]::new);
//            req.setPhoneNumberSet(phoneNums);
//            String[] par = new String[param.size()];
//            par = param.toArray(par);
//            req.setTemplateParamSet(par);
//            /* 模板参数: 若无模板参数，则设置为空 */
//            /*String[] templateParamSet = {"5678"};
//            req.setTemplateParamSet(templateParamSet);*/
//            /* 通过 client 对象调用 SendSms 方法发起请求。注意请求方法名与请求对象是对应的
//             * 返回的 res 是一个 SendSmsResponse 类的实例，与请求对象对应 */
//            SendSmsResponse res = smsClient.SendSms(req);
            // 构建请求体
            RequestBody body = RequestBody.create(buildJsonRequestBody(receivePhone,param,templateId), MediaType.parse("application/json; charset=utf-8"));

            // 构建请求
            Request request = new Request.Builder()
                    .url("sms.tencentcloudapi.com")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-TC-Action", "SendSms")
                    // 添加其他公共请求参数（如果需要）
                    .build();

            // 发送请求并处理响应
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                log.info("短信发送结果:{}", response.body().string());
                return receivePhone.size();
            }
            // 输出json格式的字符串回包
        } catch (Exception e) {
            log.error("发送短信失败", e);
            return 0;
        }
    }

    private String buildJsonRequestBody(Collection<String> phones,Collection<String> params,String templateId) {
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
