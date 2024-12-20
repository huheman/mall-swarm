package com.macro.mall.portal.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.macro.mall.mapper.OmsCartItemMapper;
import com.macro.mall.mapper.PmsSkuStockMapper;
import com.macro.mall.model.OmsCartItem;
import com.macro.mall.model.OmsCartItemExample;
import com.macro.mall.model.PmsSkuStock;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.dao.PortalProductDao;
import com.macro.mall.portal.domain.CartProduct;
import com.macro.mall.portal.domain.CartPromotionItem;
import com.macro.mall.portal.service.OmsCartItemService;
import com.macro.mall.portal.service.OmsPromotionService;
import com.macro.mall.portal.service.UmsMemberService;
import com.macro.mall.portal.service.bo.CartAttributeBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 购物车管理Service实现类
 * Created by macro on 2018/8/2.
 */
@Service
public class OmsCartItemServiceImpl implements OmsCartItemService {
    private static final Logger log = LoggerFactory.getLogger(OmsCartItemServiceImpl.class);
    @Autowired
    private OmsCartItemMapper cartItemMapper;
    @Autowired
    private PortalProductDao productDao;
    @Autowired
    private OmsPromotionService promotionService;
    @Autowired
    private UmsMemberService memberService;
    @Autowired
    private PmsSkuStockMapper skuStockMapper;

    public int updateAttribute(Long cartId, List<CartAttributeBO> cartAttributeBOList) {
        OmsCartItem omsCartItem = cartItemMapper.selectByPrimaryKey(cartId);
        JSONArray jsonArray = JSON.parseArray(omsCartItem.getProductAttr());
        Map<String, String> replaceMap = cartAttributeBOList.stream()
                .collect(Collectors.toMap(CartAttributeBO::getKey, cartAttributeBO -> cartAttributeBO.getValue(), (s, s2) -> s, (Supplier<Map<String, String>>) () -> new LinkedHashMap<>()));
        JSONArray finalAttr = new JSONArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String attrKey = jsonObject.getString("key");
            if (replaceMap.containsKey(attrKey)) {
                JSONObject unit = new JSONObject();
                unit.put("key", attrKey);
                unit.put("value", replaceMap.get(attrKey));
                finalAttr.add(unit);
                replaceMap.remove(attrKey);
            } else {
                finalAttr.add(jsonObject);
            }
        }
        for (Map.Entry<String, String> stringStringEntry : replaceMap.entrySet()) {
            JSONObject unit = new JSONObject();
            unit.put("key", stringStringEntry.getKey());
            unit.put("value", stringStringEntry.getValue());
            finalAttr.add(unit);
        }
        omsCartItem.setProductAttr(finalAttr.toString());
        // 要对一些内容进行校验
        return cartItemMapper.updateByPrimaryKeySelective(omsCartItem);
    }

    @Override
    public Long add(OmsCartItem cartItem) {
        UmsMember currentMember = memberService.getCurrentMember();
        cartItem.setMemberId(currentMember.getId());
        cartItem.setMemberNickname(currentMember.getNickname());
        cartItem.setDeleteStatus(0);
        PmsSkuStock pmsSkuStock = skuStockMapper.selectByPrimaryKey(cartItem.getProductSkuId());
        cartItem.setPrice(pmsSkuStock.getPrice());
        OmsCartItem existCartItem = getCartItem(cartItem);
        if (existCartItem == null) {
            cartItem.setCreateDate(new Date());
            cartItemMapper.insert(cartItem);
            existCartItem = cartItem;
        } else {
            cartItem.setModifyDate(new Date());
            existCartItem.setQuantity(existCartItem.getQuantity() + cartItem.getQuantity());
            cartItemMapper.updateByPrimaryKey(existCartItem);
        }
        return existCartItem.getId();
    }

    /**
     * 根据会员id,商品id和规格获取购物车中商品
     */
    private OmsCartItem getCartItem(OmsCartItem cartItem) {
        OmsCartItemExample example = new OmsCartItemExample();
        example.createCriteria().andMemberIdEqualTo(cartItem.getMemberId())
                .andProductIdEqualTo(cartItem.getProductId()).andDeleteStatusEqualTo(0)
                .andProductSkuIdEqualTo(cartItem.getProductSkuId());
        List<OmsCartItem> cartItemList = cartItemMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(cartItemList)) {
            return cartItemList.get(0);
        }
        return null;
    }

    @Override
    public List<OmsCartItem> list(Long memberId) {
        OmsCartItemExample example = new OmsCartItemExample();
        example.createCriteria().andDeleteStatusEqualTo(0).andMemberIdEqualTo(memberId);
        return cartItemMapper.selectByExample(example);
    }

    @Override
    public List<CartPromotionItem> listPromotion(Long memberId, List<Long> cartIds) {
        List<OmsCartItem> cartItemList = list(memberId);
        if (CollUtil.isNotEmpty(cartIds)) {
            cartItemList = cartItemList.stream().filter(item -> cartIds.contains(item.getId())).collect(Collectors.toList());
        }
        List<CartPromotionItem> cartPromotionItemList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cartItemList)) {
            cartPromotionItemList = promotionService.calcCartPromotion(cartItemList);
        }
        return cartPromotionItemList;
    }

    @Override
    public int updateQuantity(Long id, Long memberId, Integer quantity) {
        OmsCartItem cartItem = new OmsCartItem();
        cartItem.setQuantity(quantity);
        OmsCartItemExample example = new OmsCartItemExample();
        example.createCriteria().andDeleteStatusEqualTo(0)
                .andIdEqualTo(id).andMemberIdEqualTo(memberId);
        return cartItemMapper.updateByExampleSelective(cartItem, example);
    }

    @Override
    public int delete(Long memberId, List<Long> ids) {
        for (Long id : ids) {
            cartItemMapper.deleteByPrimaryKey(id);
        }
        return 1;
    }

    @Override
    public CartProduct getCartProduct(Long productId) {
        return productDao.getCartProduct(productId);
    }


    @Override
    public int clear(Long memberId) {
        OmsCartItemExample example = new OmsCartItemExample();
        example.createCriteria().andMemberIdEqualTo(memberId);
        return cartItemMapper.deleteByExample(example);
    }
}
