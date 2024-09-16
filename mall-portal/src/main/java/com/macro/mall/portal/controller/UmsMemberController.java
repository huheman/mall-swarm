package com.macro.mall.portal.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.hutool.json.JSONObject;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.component.SmsSender;
import com.macro.mall.portal.service.IdentityService;
import com.macro.mall.portal.service.UmsMemberService;
import com.macro.mall.portal.service.bo.IdentityInfoBO;
import com.macro.mall.portal.service.bo.IdentityResultBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 会员登录注册管理Controller
 * Created by macro on 2018/8/3.
 */
@Controller
@Tag(name = "UmsMemberController", description = "会员登录注册管理")
@RequestMapping("/sso")
public class UmsMemberController {
    @Autowired
    private SmsSender smsSender;
    @Autowired
    private UmsMemberService memberService;
    @Value("${sa-token.token-prefix}")
    private String tokenHead;
    @Autowired
    private IdentityService identityService;


    private static final String APP_ID = "wxe26bc51aa1206df9";

    @Operation(summary = "实名认证信息")
    @GetMapping("/identityInfo")
    @ResponseBody
    public CommonResult<IdentityResultBO> identityInfo() {
        IdentityResultBO identityResultBO = identityService.identityIdNumber(memberService.getCurrentMember().getId());
        return CommonResult.success(identityResultBO);
    }

    /*实名认证*/
    @Operation(summary = "实名认证")
    @PostMapping("/identity")
    @ResponseBody
    public CommonResult<Boolean> identity(@RequestBody IdentityInfoBO identityInfoBO) {
        Boolean identity = identityService.identity(memberService.getCurrentMember().getId(), identityInfoBO.getRealName(), identityInfoBO.getIdNo());
        return CommonResult.success(identity);
    }


    @Operation(summary = "会员注册")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult register(@RequestParam String username,
                                 @RequestParam String password,
                                 @RequestParam String telephone,
                                 @RequestParam String authCode) {
        memberService.register(username, password, telephone);
        return CommonResult.success(null, "注册成功");
    }

    @Operation(summary = "会员登录")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult login(@RequestParam String username,
                              @RequestParam String password) {
        SaTokenInfo saTokenInfo = memberService.login(username, password);
        if (saTokenInfo == null) {
            return CommonResult.validateFailed("用户名或密码错误");
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", saTokenInfo.getTokenValue());
        tokenMap.put("tokenHead", tokenHead + " ");
        return CommonResult.success(tokenMap);
    }

    @Operation(summary = "会员手机登录")
    @RequestMapping(value = "/loginByPhone", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult loginByPhone(@RequestParam String phone,
                                     @RequestParam String authCode) {
        SaTokenInfo saTokenInfo = memberService.loginByPhone(phone, authCode);
        if (saTokenInfo == null) {
            return CommonResult.validateFailed("手机号或验证码不对");
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", saTokenInfo.getTokenValue());
        tokenMap.put("tokenHead", tokenHead + " ");
        return CommonResult.success(tokenMap);
    }

    @Operation(summary = "获取会员信息")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult info() {
        UmsMember member = memberService.getCurrentMember();
        return CommonResult.success(member);
    }

    @Operation(summary = "登出功能")
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult logout() {
        memberService.logout();
        return CommonResult.success(null);
    }

    @Operation(summary = "获取验证码")
    @RequestMapping(value = "/getAuthCode", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult getAuthCode(@RequestParam String telephone) {
        String authCode = memberService.generateAuthCode(telephone);
        smsSender.sendAuthCode(telephone, authCode);
        return CommonResult.success( "获取验证码成功");
    }

    @Operation(summary = "修改密码")
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updatePassword(@RequestParam String telephone,
                                       @RequestParam String password,
                                       @RequestParam String authCode) {
        memberService.updatePassword(telephone, password, authCode);
        return CommonResult.success(null, "密码修改成功");
    }


}
