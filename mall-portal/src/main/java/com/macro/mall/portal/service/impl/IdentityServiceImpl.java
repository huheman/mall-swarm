package com.macro.mall.portal.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.macro.mall.mapper.UmsMemberMapper;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.service.IdentityService;
import com.macro.mall.portal.service.bo.IdentityResultBO;
import com.macro.mall.portal.util.AESUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class IdentityServiceImpl implements IdentityService {
    @Autowired
    private OkHttpClient client;
    @Value("${aliyun.app.code}")
    private String appCode;

    @Value("${aliyun.id.cert.url}")
    private String url;
    @Autowired
    private UmsMemberMapper memberMapper;

    private static final String ENCRITY_KEY = "IDENTITY_HUHP";

    @SneakyThrows
    private String postData(String name, String idNo) {
        if (StringUtils.isEmpty(idNo) || StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("身份证号码和姓名不能为空");
        }
        String result = "";
        RequestBody formBody = new FormBody.Builder().add("name", name).add("idNo", idNo).build();
        Request request = new Request.Builder().url(url).addHeader("Authorization", "APPCODE " + appCode).post(formBody).build();

        Call call = client.newCall(request);
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            log.error("执行实名校验失败了", e);
            throw e;
        }

        if (!response.isSuccessful()) {      // 当返回结果发生错误时
            // 状态码为403时一般是套餐包用尽，需续购；注意：续购不会改变秘钥（appCode），仅增加次数
            // 续购链接：https://market.aliyun.com/products/57000002/cmapi025518.html
            log.error("request failed----" + "返回状态码" + response.code() + ",message:" + response.message());
        }
        result = response.body().string();    //此处不可以使用toString()方法，该方法已过期

        return result;
    }

    @Override
    public Boolean identity(Long memberId, String realName, String idNo) {
        if (memberId == null) {
            throw new IllegalArgumentException("人员编码不能为空");
        }
        if (idNo == null || idNo.length() != 18) {
            throw new IllegalArgumentException("身份证号格式不正确");
        }

        UmsMember umsMember = memberMapper.selectByPrimaryKey(memberId);
        if (umsMember == null) {
            throw new IllegalArgumentException("未找到" + memberId + "的用户");
        }
        if (umsMember.getStatus() != 1) {
            throw new IllegalArgumentException("账号已禁用");
        }
        String s = postData(realName, idNo);
        JSONObject jsonObject = JSON.parseObject(s);
        String respCode = jsonObject.getString("respCode");
        if ("0000".equals(respCode)) {
            // 实名认证成功
            // 暂时用个性签名记录实名
            idNo = AESUtil.encrypt(idNo, ENCRITY_KEY);
            umsMember.setPersonalizedSignature(realName + ":" + idNo);
            memberMapper.updateByPrimaryKeySelective(umsMember);
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        String decrypt = AESUtil.decrypt("y/nsedzpdBCHhukaI8HXar9ziGXkIKyBFFMQtOHNnno=", ENCRITY_KEY);
        System.out.printf(decrypt);
    }

    @Override
    public IdentityResultBO identityIdNumber(Long memberId) {
        UmsMember umsMember = memberMapper.selectByPrimaryKey(memberId);
        if (umsMember == null) {
            throw new IllegalArgumentException("未找到" + memberId + "的用户");
        }
        if (umsMember.getStatus() != 1) {
            throw new IllegalArgumentException("账号已禁用");
        }
        IdentityResultBO identityResultBO = new IdentityResultBO();

        String personalizedSignature = umsMember.getPersonalizedSignature();
        if (StringUtils.isEmpty(personalizedSignature)) {
            identityResultBO.setHasIdentity(false);
        }else {
            identityResultBO.setHasIdentity(true);
            String[] split = personalizedSignature.split(":");
            String idNo = AESUtil.decrypt(split[1], ENCRITY_KEY);
            identityResultBO.setIdNo(idNo);
            identityResultBO.setRealName(split[0]);
        }
        return identityResultBO;
    }
}
