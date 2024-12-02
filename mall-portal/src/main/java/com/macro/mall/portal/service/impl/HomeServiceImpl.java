package com.macro.mall.portal.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.github.pagehelper.PageHelper;
import com.macro.mall.mapper.*;
import com.macro.mall.model.*;
import com.macro.mall.portal.dao.HomeDao;
import com.macro.mall.portal.dao.PortalOrderDao;
import com.macro.mall.portal.domain.FlashPromotionProduct;
import com.macro.mall.portal.domain.HomeContentResult;
import com.macro.mall.portal.domain.HomeFlashPromotion;
import com.macro.mall.portal.service.HomeService;
import com.macro.mall.portal.service.bo.MemberProductBO;
import com.macro.mall.portal.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 首页内容管理Service实现类
 * Created by macro on 2019/1/28.
 */
@Slf4j
@Service
public class HomeServiceImpl implements HomeService {
    @Autowired
    private SmsHomeAdvertiseMapper advertiseMapper;
    @Autowired
    private HomeDao homeDao;
    @Autowired
    private SmsFlashPromotionMapper flashPromotionMapper;
    @Autowired
    private SmsFlashPromotionSessionMapper promotionSessionMapper;
    @Autowired
    private PmsProductMapper productMapper;
    @Autowired
    private PmsProductCategoryMapper productCategoryMapper;
    @Autowired
    private CmsSubjectMapper subjectMapper;
    @Value("${game.product.parent.id}")
    private Long gameProductParentId;
    @Autowired
    private PortalOrderDao portalOrderDao;
    @Autowired
    private UmsMemberMapper umsMemberMapper;

    @Autowired
    private OmsOrderMapper orderMapper;

    @NacosValue(value = "${app.fake.order}",autoRefreshed = true)
    private Long fakeOrderCount;

    @NacosValue(value = "${app.fake.user}",autoRefreshed = true)
    private Long fakeUserCount;

    @NacosValue(value = "${app.fake.game}",autoRefreshed = true)
    private String fakeGames;


    @Override
    public HomeContentResult content() {
        HomeContentResult result = new HomeContentResult();
        //获取首页广告
        result.setAdvertiseList(getHomeAdvertiseList());
        // 获取游戏，目前所有游戏都在56这个一级目录下
        result.setGameList(getProductCateList(gameProductParentId));
        result.setOrderCount(getOrderCount());
        result.setUserCount(getUserCount());
        result.setLastBuyList(lastBuyList());
        //获取推荐品牌
        // result.setBrandList(homeDao.getRecommendBrandList(0,6));
        //获取秒杀信息
        // result.setHomeFlashPromotion(getHomeFlashPromotion());
        //获取新品推荐
        // result.setNewProductList(homeDao.getNewProductList(0,4));
        //获取人气推荐
        // result.setHotProductList(homeDao.getHotProductList(0,4));
        //获取推荐专题
        // result.setSubjectList(homeDao.getRecommendSubjectList(0,4));
        return result;
    }

    private List<HomeContentResult.ResultUnit> lastBuyList() {
        List<HomeContentResult.ResultUnit> result = new ArrayList<>();

        if (StringUtils.hasText(fakeGames)) {

            try {
                Random random = new Random();
                for (String s : fakeGames.split(",")) {
                    String gameName = s.split("-")[0].trim();
                    String price = s.split("-")[1].trim();
                    HomeContentResult.ResultUnit unit = new HomeContentResult.ResultUnit();
                    unit.setUserName(RandomStringUtils.randomAlphabetic(10));
                    unit.setGameName(gameName);
                    int randomNumber = random.nextInt(1800 - 1000 + 1) + 1000;
                    unit.setMoney(new BigDecimal(price));
                    unit.setBuyTime(new Date(System.currentTimeMillis() - 1000 * randomNumber));
                    result.add(unit);
                }
            } catch (Exception e) {
                log.error("生成虚拟订单失败", e);
            }
        }
        OmsOrderExample example = new OmsOrderExample();
        example.createCriteria().andStatusEqualTo(3);
        example.setOrderByClause("id desc limit 9");
        List<OmsOrder> omsOrders = orderMapper.selectByExample(example);
        List<HomeContentResult.ResultUnit> list = omsOrders.stream().map(new Function<OmsOrder, HomeContentResult.ResultUnit>() {
            @Override
            public HomeContentResult.ResultUnit apply(OmsOrder omsOrder) {
                HomeContentResult.ResultUnit unit = new HomeContentResult.ResultUnit();
                String moreInfo = omsOrder.getMoreInfo();
                if (StringUtils.hasText(moreInfo)) {
                    JSONObject jsonObject = JSONObject.parseObject(moreInfo);
                    unit.setUserName(jsonObject.getString("userName"));
                    unit.setGameName(jsonObject.getString("gameName"));
                }

                unit.setMoney(omsOrder.getPayAmount());
                unit.setBuyTime(omsOrder.getPaymentTime());
                return unit;
            }
        }).toList();
        result.addAll(list);
        return result;
    }

    private Long getUserCount() {


        UmsMemberExample umsMemberExample = new UmsMemberExample();
        long count = umsMemberMapper.countByExample(umsMemberExample);
        if (fakeUserCount != null && fakeUserCount > 0) {
            count += fakeUserCount;
        }
        return count;
    }

    private Long getOrderCount() {

        Long count = portalOrderDao.count(3, null);
        if (fakeOrderCount != null && fakeOrderCount > 0) {
            count += fakeOrderCount;
        }
        return count;
    }

    @Override
    public List<PmsProduct> recommendProductList(Integer pageSize, Integer pageNum) {
        // TODO: 2019/1/29 暂时默认推荐所有商品
        PageHelper.startPage(pageNum, pageSize);
        PmsProductExample example = new PmsProductExample();
        example.createCriteria()
                .andDeleteStatusEqualTo(0)
                .andPublishStatusEqualTo(1);
        return productMapper.selectByExample(example);
    }

    @Override
    public List<PmsProductCategory> getProductCateList(Long parentId) {
        PmsProductCategoryExample example = new PmsProductCategoryExample();
        example.createCriteria()
                .andShowStatusEqualTo(1)
                .andParentIdEqualTo(parentId);
        example.setOrderByClause("sort desc");
        return productCategoryMapper.selectByExample(example);
    }

    @Override
    public List<CmsSubject> getSubjectList(Long cateId, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        CmsSubjectExample example = new CmsSubjectExample();
        CmsSubjectExample.Criteria criteria = example.createCriteria();
        criteria.andShowStatusEqualTo(1);
        if (cateId != null) {
            criteria.andCategoryIdEqualTo(cateId);
        }
        return subjectMapper.selectByExample(example);
    }

    @Override
    public List<PmsProduct> hotProductList(Integer pageNum, Integer pageSize) {
        int offset = pageSize * (pageNum - 1);
        return homeDao.getHotProductList(offset, pageSize);
    }

    @Override
    public List<PmsProduct> newProductList(Integer pageNum, Integer pageSize) {
        int offset = pageSize * (pageNum - 1);
        return homeDao.getNewProductList(offset, pageSize);
    }

    @Override
    public MemberProductBO hotGameList(Long userId) {
        MemberProductBO memberProductBO = new MemberProductBO();
        PmsProductCategoryExample example = new PmsProductCategoryExample();
        example.createCriteria()
                .andNavStatusEqualTo(1)
                .andParentIdEqualTo(gameProductParentId);
        example.setOrderByClause("sort desc ,id desc limit 5");

        List<PmsProductCategory> hotGames = productCategoryMapper.selectByExample(example);
        memberProductBO.setHotGames(hotGames);
        Map<String, List<PmsProductCategory>> allMap = getProductCateList(gameProductParentId)
                .stream()
                .filter(pmsProductCategory -> pmsProductCategory.getKeywords() != null && !pmsProductCategory.getKeywords().isEmpty())
                .collect(Collectors.groupingBy(PmsProductCategory::getKeywords));
        memberProductBO.setAllGames(new TreeMap<>(allMap));
        if (userId != null) {
            List<PmsProductCategory> historyGames = homeDao.findHistoryGame(userId, gameProductParentId);
            memberProductBO.setHistoryGames(historyGames);
        }
        return memberProductBO;
    }

    private HomeFlashPromotion getHomeFlashPromotion() {
        HomeFlashPromotion homeFlashPromotion = new HomeFlashPromotion();
        //获取当前秒杀活动
        Date now = new Date();
        SmsFlashPromotion flashPromotion = getFlashPromotion(now);
        if (flashPromotion != null) {
            //获取当前秒杀场次
            SmsFlashPromotionSession flashPromotionSession = getFlashPromotionSession(now);
            if (flashPromotionSession != null) {
                homeFlashPromotion.setStartTime(flashPromotionSession.getStartTime());
                homeFlashPromotion.setEndTime(flashPromotionSession.getEndTime());
                //获取下一个秒杀场次
                SmsFlashPromotionSession nextSession = getNextFlashPromotionSession(homeFlashPromotion.getStartTime());
                if (nextSession != null) {
                    homeFlashPromotion.setNextStartTime(nextSession.getStartTime());
                    homeFlashPromotion.setNextEndTime(nextSession.getEndTime());
                }
                //获取秒杀商品
                List<FlashPromotionProduct> flashProductList = homeDao.getFlashProductList(flashPromotion.getId(), flashPromotionSession.getId());
                homeFlashPromotion.setProductList(flashProductList);
            }
        }
        return homeFlashPromotion;
    }

    //获取下一个场次信息
    private SmsFlashPromotionSession getNextFlashPromotionSession(Date date) {
        SmsFlashPromotionSessionExample sessionExample = new SmsFlashPromotionSessionExample();
        sessionExample.createCriteria()
                .andStartTimeGreaterThan(date);
        sessionExample.setOrderByClause("start_time asc");
        List<SmsFlashPromotionSession> promotionSessionList = promotionSessionMapper.selectByExample(sessionExample);
        if (!CollectionUtils.isEmpty(promotionSessionList)) {
            return promotionSessionList.get(0);
        }
        return null;
    }

    private List<SmsHomeAdvertise> getHomeAdvertiseList() {
        SmsHomeAdvertiseExample example = new SmsHomeAdvertiseExample();
        example.createCriteria().andStatusEqualTo(1);
        example.setOrderByClause("sort desc ,id desc");
        return advertiseMapper.selectByExample(example);
    }

    //根据时间获取秒杀活动
    private SmsFlashPromotion getFlashPromotion(Date date) {
        Date currDate = DateUtil.getDate(date);
        SmsFlashPromotionExample example = new SmsFlashPromotionExample();
        example.createCriteria()
                .andStatusEqualTo(1)
                .andStartDateLessThanOrEqualTo(currDate)
                .andEndDateGreaterThanOrEqualTo(currDate);
        List<SmsFlashPromotion> flashPromotionList = flashPromotionMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(flashPromotionList)) {
            return flashPromotionList.get(0);
        }
        return null;
    }

    //根据时间获取秒杀场次
    private SmsFlashPromotionSession getFlashPromotionSession(Date date) {
        Date currTime = DateUtil.getTime(date);
        SmsFlashPromotionSessionExample sessionExample = new SmsFlashPromotionSessionExample();
        sessionExample.createCriteria()
                .andStartTimeLessThanOrEqualTo(currTime)
                .andEndTimeGreaterThanOrEqualTo(currTime);
        List<SmsFlashPromotionSession> promotionSessionList = promotionSessionMapper.selectByExample(sessionExample);
        if (!CollectionUtils.isEmpty(promotionSessionList)) {
            return promotionSessionList.get(0);
        }
        return null;
    }
}
