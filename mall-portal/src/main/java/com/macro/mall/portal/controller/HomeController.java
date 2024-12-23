package com.macro.mall.portal.controller;

import com.alibaba.fastjson.JSON;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.common.service.RedisService;
import com.macro.mall.model.CmsSubject;
import com.macro.mall.model.PmsProduct;
import com.macro.mall.model.PmsProductCategory;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.config.AppConfig;
import com.macro.mall.portal.controller.vo.RedeemInfoVO;
import com.macro.mall.portal.domain.HomeContentResult;
import com.macro.mall.portal.service.HomeService;
import com.macro.mall.portal.service.RedeemService;
import com.macro.mall.portal.service.UmsMemberService;
import com.macro.mall.portal.service.bo.MemberProductBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 首页内容管理Controller
 * Created by macro on 2019/1/28.
 */
@Controller
@Tag(name = "HomeController", description = "首页内容管理")
@RequestMapping("/home")
public class HomeController {
    @Autowired
    private HomeService homeService;
    @Autowired
    private UmsMemberService memberService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private RedeemService redeemService;

    @GetMapping("/blockIOS")
    @ResponseBody
    public CommonResult<Boolean> blockIOS() {
        Boolean blockIOS = appConfig.getBlockIOS();
        return CommonResult.success(blockIOS);
    }

    @GetMapping("redeemInfo")
    @ResponseBody
    public CommonResult<RedeemInfoVO> redeemInfo(String redeemCode) {
        try {
            RedeemInfoVO redeemInfoVO = redeemService.info(redeemCode);
            return CommonResult.success(redeemInfoVO);
        } catch (Exception e) {
            return CommonResult.failed("兑换失败");
        }
    }

    @Operation(summary = "首页内容页信息展示")
    @RequestMapping(value = "/content", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<HomeContentResult> content() {
        String homeContent = (String) redisService.get("homeContent");
        HomeContentResult content;
        if (!StringUtils.hasLength(homeContent)) {
            content = homeService.content();
            String jsonString = JSON.toJSONString(content);
            redisService.set("homeContent", jsonString, 60);
        } else {
            content = JSON.parseObject(homeContent, HomeContentResult.class);
        }

        return CommonResult.success(content);
    }

    @GetMapping("/hotGameList")
    @ResponseBody
    public CommonResult<MemberProductBO> getHotGameList() {
        Long userId = null;
        try {
            UmsMember currentMember = memberService.getCurrentMember();
            userId = Optional.ofNullable(currentMember)
                    .map(UmsMember::getId).orElse(null);
        } catch (Exception ignore) {

        }

        MemberProductBO memberProductBO = homeService.hotGameList(userId);
        return CommonResult.success(memberProductBO);
    }

    @Operation(summary = "分页获取推荐商品")
    @RequestMapping(value = "/recommendProductList", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<PmsProduct>> recommendProductList(@RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize,
                                                               @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<PmsProduct> productList = homeService.recommendProductList(pageSize, pageNum);
        return CommonResult.success(productList);
    }

    @Operation(summary = "获取首页商品分类")
    @RequestMapping(value = "/productCateList/{parentId}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<PmsProductCategory>> getProductCateList(@PathVariable Long parentId) {
        List<PmsProductCategory> productCategoryList = homeService.getProductCateList(parentId);
        return CommonResult.success(productCategoryList);
    }

    @Operation(summary = "根据分类获取专题")
    @RequestMapping(value = "/subjectList", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<CmsSubject>> getSubjectList(@RequestParam(required = false) Long cateId,
                                                         @RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize,
                                                         @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<CmsSubject> subjectList = homeService.getSubjectList(cateId, pageSize, pageNum);
        return CommonResult.success(subjectList);
    }

    @Operation(summary = "分页获取人气推荐商品")
    @RequestMapping(value = "/hotProductList", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<PmsProduct>> hotProductList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                         @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize) {
        List<PmsProduct> productList = homeService.hotProductList(pageNum, pageSize);
        return CommonResult.success(productList);
    }

    @Operation(summary = "分页获取新品推荐商品")
    @RequestMapping(value = "/newProductList", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<PmsProduct>> newProductList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                         @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize) {
        List<PmsProduct> productList = homeService.newProductList(pageNum, pageSize);
        return CommonResult.success(productList);
    }
}
