package com.macro.mall.portal.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.macro.mall.common.exception.Asserts;
import com.macro.mall.mapper.*;
import com.macro.mall.model.*;
import com.macro.mall.portal.dao.SmsCouponHistoryDao;
import com.macro.mall.portal.domain.CartPromotionItem;
import com.macro.mall.portal.domain.SmsCouponHistoryDetail;
import com.macro.mall.portal.service.UmsMemberCouponService;
import com.macro.mall.portal.service.UmsMemberService;
import com.macro.mall.portal.service.bo.CouponReceiveBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 会员优惠券管理Service实现类
 * Created by macro on 2018/8/29.
 */
@Service
public class UmsMemberCouponServiceImpl implements UmsMemberCouponService {
    @Autowired
    private UmsMemberService memberService;
    @Autowired
    private SmsCouponMapper couponMapper;
    @Autowired
    private SmsCouponHistoryMapper couponHistoryMapper;
    @Autowired
    private SmsCouponHistoryDao couponHistoryDao;
    @Autowired
    private SmsCouponProductRelationMapper couponProductRelationMapper;
    @Autowired
    private SmsCouponProductCategoryRelationMapper couponProductCategoryRelationMapper;
    @Autowired
    private PmsProductMapper productMapper;

    @Override
    public void add(Long couponId, Long userId) {
        UmsMember currentMember;
        if (userId == null) {
            currentMember = memberService.getCurrentMember();

        } else {
            currentMember = memberService.getById(userId);
        }
        //获取优惠券信息，判断数量
        SmsCoupon coupon = couponMapper.selectByPrimaryKey(couponId);
        if (coupon == null) {
            Asserts.fail("优惠券不存在");
        }
        if (coupon.getCount() <= 0) {
            Asserts.fail("优惠券已经领完了");
        }
        Date now = new Date();
        if (now.before(coupon.getEnableTime())) {
            Asserts.fail("优惠券还没到领取时间");
        }
        //判断用户领取的优惠券数量是否超过限制
        SmsCouponHistoryExample couponHistoryExample = new SmsCouponHistoryExample();
        couponHistoryExample.createCriteria().andCouponIdEqualTo(couponId).andMemberIdEqualTo(currentMember.getId());
        long count = couponHistoryMapper.countByExample(couponHistoryExample);
        if (count >= coupon.getPerLimit()) {
            Asserts.fail("您已经领取过该优惠券");
        }
        //生成领取优惠券历史
        SmsCouponHistory couponHistory = new SmsCouponHistory();
        couponHistory.setCouponId(couponId);
        couponHistory.setCouponCode(generateCouponCode(currentMember.getId()));
        couponHistory.setCreateTime(now);
        couponHistory.setMemberId(currentMember.getId());
        couponHistory.setMemberNickname(currentMember.getNickname());
        //主动领取
        couponHistory.setGetType(1);
        //未使用
        couponHistory.setUseStatus(0);
        couponHistoryMapper.insert(couponHistory);
        //修改优惠券表的数量、领取数量
        coupon.setCount(coupon.getCount() - 1);
        coupon.setReceiveCount(coupon.getReceiveCount() == null ? 1 : coupon.getReceiveCount() + 1);
        couponMapper.updateByPrimaryKey(coupon);
    }

    /**
     * 16位优惠码生成：时间戳后8位+4位随机数+用户id后4位
     */
    private String generateCouponCode(Long memberId) {
        StringBuilder sb = new StringBuilder();
        Long currentTimeMillis = System.currentTimeMillis();
        String timeMillisStr = currentTimeMillis.toString();
        sb.append(timeMillisStr.substring(timeMillisStr.length() - 8));
        for (int i = 0; i < 4; i++) {
            sb.append(new Random().nextInt(10));
        }
        String memberIdStr = memberId.toString();
        if (memberIdStr.length() <= 4) {
            sb.append(String.format("%04d", memberId));
        } else {
            sb.append(memberIdStr.substring(memberIdStr.length() - 4));
        }
        return sb.toString();
    }

    @Override
    public List<SmsCouponHistory> listHistory(Integer useStatus) {
        UmsMember currentMember = memberService.getCurrentMember();
        SmsCouponHistoryExample couponHistoryExample = new SmsCouponHistoryExample();
        SmsCouponHistoryExample.Criteria criteria = couponHistoryExample.createCriteria();
        criteria.andMemberIdEqualTo(currentMember.getId());
        if (useStatus != null) {
            criteria.andUseStatusEqualTo(useStatus);
        }
        return couponHistoryMapper.selectByExample(couponHistoryExample);
    }

    public Map<Boolean, List<SmsCouponHistoryDetail>> listCartAll(List<CartPromotionItem> cartItemList) {
        UmsMember currentMember = memberService.getCurrentMember();
        Date now = new Date();
        //获取该用户所有优惠券
        List<SmsCouponHistoryDetail> allList = couponHistoryDao.getDetailList(currentMember.getId())
                .stream().filter(smsCouponHistoryDetail -> smsCouponHistoryDetail.getCoupon() != null)
                .toList();
        //根据优惠券使用类型来判断优惠券是否可用
        List<SmsCouponHistoryDetail> enableList = new ArrayList<>();
        List<SmsCouponHistoryDetail> disableList = new ArrayList<>();
        for (SmsCouponHistoryDetail couponHistoryDetail : allList) {
            Integer useType = couponHistoryDetail.getCoupon().getUseType();
            BigDecimal minPoint = couponHistoryDetail.getCoupon().getMinPoint();
            Date endTime = couponHistoryDetail.getCoupon().getEndTime();
            if (useType.equals(0)) {
                //0->全场通用
                //判断是否满足优惠起点
                //计算购物车商品的总价
                BigDecimal totalAmount = calcTotalAmount(cartItemList);
                if (now.before(endTime) && totalAmount.subtract(minPoint).intValue() >= 0) {
                    enableList.add(couponHistoryDetail);
                } else {
                    disableList.add(couponHistoryDetail);
                }
            } else if (useType.equals(1)) {
                //1->指定分类
                //计算指定分类商品的总价
                List<Long> productCategoryIds = new ArrayList<>();
                for (SmsCouponProductCategoryRelation categoryRelation : couponHistoryDetail.getCategoryRelationList()) {
                    productCategoryIds.add(categoryRelation.getProductCategoryId());
                }
                BigDecimal totalAmount = calcTotalAmountByproductCategoryId(cartItemList, productCategoryIds);
                if (now.before(endTime) && totalAmount.intValue() > 0 && totalAmount.subtract(minPoint).intValue() >= 0) {
                    enableList.add(couponHistoryDetail);
                } else {
                    disableList.add(couponHistoryDetail);
                }
            } else if (useType.equals(2)) {
                //2->指定商品
                //计算指定商品的总价
                List<Long> productIds = new ArrayList<>();
                for (SmsCouponProductRelation productRelation : couponHistoryDetail.getProductRelationList()) {
                    productIds.add(productRelation.getProductId());
                }
                BigDecimal totalAmount = calcTotalAmountByProductId(cartItemList, productIds);
                if (now.before(endTime) && totalAmount.intValue() > 0 && totalAmount.subtract(minPoint).intValue() >= 0) {
                    enableList.add(couponHistoryDetail);
                } else {
                    disableList.add(couponHistoryDetail);
                }
            }
        }
        Map<Boolean, List<SmsCouponHistoryDetail>> result = new HashMap<>();
        result.put(true, enableList);
        result.put(false, disableList);
        return result;
    }

    @Override
    public List<SmsCouponHistoryDetail> listCart(List<CartPromotionItem> cartItemList, Integer type) {
        Map<Boolean, List<SmsCouponHistoryDetail>> booleanListMap = listCartAll(cartItemList);
        if (type.equals(1)) {
            return booleanListMap.get(true);
        } else {
            return booleanListMap.get(false);
        }
    }

    @Override
    public List<SmsCoupon> listByProduct(Long productId) {
        List<Long> allCouponIds = new ArrayList<>();
        //获取指定商品优惠券
        SmsCouponProductRelationExample cprExample = new SmsCouponProductRelationExample();
        cprExample.createCriteria().andProductIdEqualTo(productId);
        List<SmsCouponProductRelation> cprList = couponProductRelationMapper.selectByExample(cprExample);
        if (CollUtil.isNotEmpty(cprList)) {
            List<Long> couponIds = cprList.stream().map(SmsCouponProductRelation::getCouponId).collect(Collectors.toList());
            allCouponIds.addAll(couponIds);
        }
        //获取指定分类优惠券
        PmsProduct product = productMapper.selectByPrimaryKey(productId);
        SmsCouponProductCategoryRelationExample cpcrExample = new SmsCouponProductCategoryRelationExample();
        cpcrExample.createCriteria().andProductCategoryIdEqualTo(product.getProductCategoryId());
        List<SmsCouponProductCategoryRelation> cpcrList = couponProductCategoryRelationMapper.selectByExample(cpcrExample);
        if (CollUtil.isNotEmpty(cpcrList)) {
            List<Long> couponIds = cpcrList.stream().map(SmsCouponProductCategoryRelation::getCouponId).collect(Collectors.toList());
            allCouponIds.addAll(couponIds);
        }
//        if(CollUtil.isEmpty(allCouponIds)){
//            return new ArrayList<>();
//        }
        //所有优惠券
        SmsCouponExample couponExample = new SmsCouponExample();
        couponExample.createCriteria().andEndTimeGreaterThan(new Date())
                .andStartTimeLessThan(new Date())
                .andUseTypeEqualTo(0);
        couponExample.or(couponExample.createCriteria()
                .andEndTimeGreaterThan(new Date())
                .andStartTimeLessThan(new Date())
                .andUseTypeNotEqualTo(0)
                .andIdIn(allCouponIds));
        return couponMapper.selectByExample(couponExample);
    }

    public List<SmsCoupon> listByMember(Long member, Integer type) {
        if (member == null || type == null) {
            return Collections.emptyList();
        }
        SmsCouponExample couponExample = new SmsCouponExample();
        couponExample.createCriteria()
                .andStartTimeLessThan(new Date())
                .andNoteNotEqualTo("已失效")
                // 全场赠券
                .andTypeEqualTo(type)
                .andEnableTimeLessThan(new Date());
        List<SmsCoupon> smsCoupons = couponMapper.selectByExample(couponExample);
        if (smsCoupons.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> couponIds = smsCoupons.stream().map(SmsCoupon::getId).distinct().toList();
        List<CouponReceiveBO> receiveCount = couponHistoryDao.countHistory(couponIds, member);
        Map<Long, Long> map = new HashMap<>();
        for (CouponReceiveBO couponReceiveBO : receiveCount) {
            map.put(couponReceiveBO.getCouponId(), couponReceiveBO.getReceiveCount());
        }
        return smsCoupons.stream().filter(smsCoupon -> smsCoupon.getPerLimit() > map.getOrDefault(smsCoupon.getId(), 0L)).toList();
    }

    @Override
    public List<SmsCoupon> list(Integer useStatus) {
        UmsMember member = memberService.getCurrentMember();
        return couponHistoryDao.getCouponList(member.getId(), useStatus);
    }

    private BigDecimal calcTotalAmount(List<CartPromotionItem> cartItemList) {
        BigDecimal total = new BigDecimal("0");
        for (CartPromotionItem item : cartItemList) {
            BigDecimal realPrice = item.getPrice().subtract(item.getReduceAmount());
            total = total.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
        }
        return total;
    }

    private BigDecimal calcTotalAmountByproductCategoryId(List<CartPromotionItem> cartItemList, List<Long> productCategoryIds) {
        BigDecimal total = new BigDecimal("0");
        for (CartPromotionItem item : cartItemList) {
            if (productCategoryIds.contains(item.getProductCategoryId())) {
                BigDecimal realPrice = item.getPrice().subtract(item.getReduceAmount());
                total = total.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
            }
        }
        return total;
    }

    private BigDecimal calcTotalAmountByProductId(List<CartPromotionItem> cartItemList, List<Long> productIds) {
        BigDecimal total = new BigDecimal("0");
        for (CartPromotionItem item : cartItemList) {
            if (productIds.contains(item.getProductId())) {
                BigDecimal realPrice = item.getPrice().subtract(item.getReduceAmount());
                total = total.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
            }
        }
        return total;
    }

}
