package com.macro.mall.portal.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.macro.mall.mapper.*;
import com.macro.mall.model.*;
import com.macro.mall.portal.domain.PmsPortalProductDetail;
import com.macro.mall.portal.domain.PmsProductCategoryNode;
import com.macro.mall.portal.service.HomeService;
import com.macro.mall.portal.service.PmsPortalProductService;
import com.macro.mall.portal.service.bo.HotGameBO;
import com.macro.mall.portal.service.bo.MemberProductBO;
import com.macro.mall.portal.service.bo.ProductSkuBO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 前台订单管理Service实现类
 * Created by macro on 2020/4/6.
 */
@Service
public class PmsPortalProductServiceImpl implements PmsPortalProductService {
    @Autowired
    private PmsProductMapper productMapper;
    @Autowired
    private PmsProductCategoryMapper productCategoryMapper;
    @Autowired
    private PmsBrandMapper brandMapper;
    @Autowired
    private PmsProductAttributeMapper productAttributeMapper;
    @Autowired
    private PmsProductAttributeValueMapper productAttributeValueMapper;
    @Autowired
    private PmsSkuStockMapper skuStockMapper;
    @Autowired
    private PmsProductLadderMapper productLadderMapper;
    @Autowired
    private PmsProductFullReductionMapper productFullReductionMapper;
    @Autowired
    private HomeService homeService;

    @Override
    public List<PmsProduct> search(String keyword, Long brandId, Long productCategoryId, Integer pageNum, Integer pageSize, Integer sort) {
        PageHelper.startPage(pageNum, pageSize);
        PmsProductExample example = new PmsProductExample();
        PmsProductExample.Criteria criteria = example.createCriteria();
        criteria.andDeleteStatusEqualTo(0);
        if (StrUtil.isNotEmpty(keyword)) {
            criteria.andNameLike("%" + keyword + "%");
        }
        if (brandId != null) {
            criteria.andBrandIdEqualTo(brandId);
        }
        if (productCategoryId != null) {
            criteria.andProductCategoryIdEqualTo(productCategoryId);
        }
        //1->按新品；2->按销量；3->价格从低到高；4->价格从高到低
        if (sort == 1) {
            example.setOrderByClause("id desc");
        } else if (sort == 2) {
            example.setOrderByClause("sale desc");
        } else if (sort == 3) {
            example.setOrderByClause("price asc");
        } else if (sort == 4) {
            example.setOrderByClause("price desc");
        }
        return productMapper.selectByExample(example);
    }

    @Override
    public List<PmsProductCategoryNode> categoryTreeList() {
        PmsProductCategoryExample example = new PmsProductCategoryExample();
        List<PmsProductCategory> allList = productCategoryMapper.selectByExample(example);
        List<PmsProductCategoryNode> result = allList.stream()
                .filter(item -> item.getParentId().equals(0L))
                .map(item -> covert(item, allList)).collect(Collectors.toList());
        return result;
    }

    @Override
    public PmsPortalProductDetail detail(Long id) {
        PmsPortalProductDetail result = new PmsPortalProductDetail();
        //获取商品信息
        PmsProduct product = productMapper.selectByPrimaryKey(id);
        result.setProduct(product);
        //获取品牌信息
        PmsBrand brand = brandMapper.selectByPrimaryKey(product.getBrandId());
        result.setBrand(brand);
        //获取商品属性信息
        PmsProductAttributeExample attributeExample = new PmsProductAttributeExample();
        attributeExample.createCriteria().andProductAttributeCategoryIdEqualTo(product.getProductAttributeCategoryId());
        List<PmsProductAttribute> productAttributeList = productAttributeMapper.selectByExample(attributeExample);
        result.setProductAttributeList(productAttributeList);
        //获取商品属性值信息
        if (CollUtil.isNotEmpty(productAttributeList)) {
            List<Long> attributeIds = productAttributeList.stream().map(PmsProductAttribute::getId).collect(Collectors.toList());
            PmsProductAttributeValueExample attributeValueExample = new PmsProductAttributeValueExample();
            attributeValueExample.createCriteria().andProductIdEqualTo(product.getId())
                    .andProductAttributeIdIn(attributeIds);
            List<PmsProductAttributeValue> productAttributeValueList = productAttributeValueMapper.selectByExample(attributeValueExample);
            result.setProductAttributeValueList(productAttributeValueList);
        }
        //获取商品SKU库存信息
        PmsSkuStockExample skuExample = new PmsSkuStockExample();
        skuExample.createCriteria().andProductIdEqualTo(product.getId());
        List<PmsSkuStock> skuStockList = skuStockMapper.selectByExample(skuExample);
        result.setSkuStockList(skuStockList);
        //商品阶梯价格设置
        if (product.getPromotionType() == 3) {
            PmsProductLadderExample ladderExample = new PmsProductLadderExample();
            ladderExample.createCriteria().andProductIdEqualTo(product.getId());
            List<PmsProductLadder> productLadderList = productLadderMapper.selectByExample(ladderExample);
            result.setProductLadderList(productLadderList);
        }
        //商品满减价格设置
        if (product.getPromotionType() == 4) {
            PmsProductFullReductionExample fullReductionExample = new PmsProductFullReductionExample();
            fullReductionExample.createCriteria().andProductIdEqualTo(product.getId());
            List<PmsProductFullReduction> productFullReductionList = productFullReductionMapper.selectByExample(fullReductionExample);
            result.setProductFullReductionList(productFullReductionList);
        }
        //商品可用优惠券
//        result.setCouponList(portalProductDao.getAvailableCouponList(product.getId(), product.getProductCategoryId()));
        return result;
    }

    @Override
    public List<ProductSkuBO> detailByCategory(Long categoryId) {
        PmsProductExample productExample = new PmsProductExample();
        PmsProductExample.Criteria criteria = productExample.createCriteria();
        criteria.andDeleteStatusEqualTo(0);
        criteria.andPublishStatusEqualTo(1);
        criteria.andProductCategoryIdEqualTo(categoryId);
        productExample.setOrderByClause("sort desc, id asc");
        List<PmsProduct> pmsProducts = productMapper.selectByExample(productExample);
        List<Long> ids = pmsProducts.stream().map(pmsProduct -> pmsProduct.getId()).toList();
        PmsSkuStockExample pmsSkuStockExample = new PmsSkuStockExample();
        pmsSkuStockExample.createCriteria().andProductIdIn(ids)
                .andStockGreaterThan(10);
        List<PmsSkuStock> pmsSkuStocks = skuStockMapper.selectByExample(pmsSkuStockExample);
        Map<Long, List<PmsSkuStock>> collect = pmsSkuStocks.stream()
                .collect(Collectors.groupingBy(pmsSkuStock -> pmsSkuStock.getProductId()));
        return pmsProducts.parallelStream()
                .map(pmsProduct -> asSkuBO(pmsProduct, collect.getOrDefault(pmsProduct.getId(), Collections.EMPTY_LIST))).toList();
    }

    @Override
    public HotGameBO hotGame(Long memberId) {
        MemberProductBO memberProductBO = homeService.hotGameList(memberId);
        if (!CollectionUtils.isEmpty(memberProductBO.getHistoryGames())) {
            return memberProductBO.getHistoryGames().stream()
                    .limit(1).map(pmsProductCategory -> {
                        HotGameBO hotGameBO = new HotGameBO();
                        hotGameBO.setGameName(pmsProductCategory.getName());
                        hotGameBO.setGamePic(pmsProductCategory.getIcon());
                        hotGameBO.setSid(pmsProductCategory.getId());

                        return hotGameBO;
                    }).findFirst().orElse(null);
        } else {
            return memberProductBO.getHotGames().stream()
                    .limit(1).map(pmsProductCategory -> {
                        HotGameBO hotGameBO = new HotGameBO();
                        hotGameBO.setGameName(pmsProductCategory.getName());
                        hotGameBO.setGamePic(pmsProductCategory.getIcon());
                        hotGameBO.setSid(pmsProductCategory.getId());
                        return hotGameBO;
                    }).findFirst().orElse(null);
        }
    }

    private ProductSkuBO asSkuBO(PmsProduct pmsProduct, List<PmsSkuStock> pmsSkuStocks) {

        List<ProductSkuBO.SkuBO> list = pmsSkuStocks.parallelStream()
                .map(pmsSkuStock -> {
                    ProductSkuBO.SkuBO skuBO = new ProductSkuBO.SkuBO();
                    skuBO.setProductId(pmsProduct.getId());
                    skuBO.setSkuCode(pmsSkuStock.getSkuCode());
                    skuBO.setPrice(pmsSkuStock.getPrice());
                    skuBO.setId(pmsSkuStock.getId());
                    skuBO.setSpData(pmsSkuStock.getSpData());
                    // 如果处于促销时间段内，则显示促销
                    if (pmsProduct.inQuickPromotion()) {
                        skuBO.setPromotionPrice(pmsSkuStock.getPromotionPrice());
                    }
                    return skuBO;
                }).toList();
        ProductSkuBO productSkuBO = new ProductSkuBO();
        productSkuBO.setProductId(pmsProduct.getId());
        productSkuBO.setPic(pmsProduct.getPic());
        productSkuBO.setSubTitle(pmsProduct.getSubTitle());
        productSkuBO.setBrandName(pmsProduct.getBrandName());
        productSkuBO.setName(pmsProduct.getName());
        productSkuBO.setSkuStockList(list);
        return productSkuBO;
    }


    /**
     * 初始对象转化为节点对象
     */
    private PmsProductCategoryNode covert(PmsProductCategory item, List<PmsProductCategory> allList) {
        PmsProductCategoryNode node = new PmsProductCategoryNode();
        BeanUtils.copyProperties(item, node);
        List<PmsProductCategoryNode> children = allList.stream()
                .filter(subItem -> subItem.getParentId().equals(item.getId()))
                .map(subItem -> covert(subItem, allList)).collect(Collectors.toList());
        node.setChildren(children);
        return node;
    }
}
