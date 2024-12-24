package com.macro.mall.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.macro.mall.common.api.CommonPage;
import com.macro.mall.dto.FullRedeemCodeRecordDTO;
import com.macro.mall.dto.RedeemSearchVO;
import com.macro.mall.mapper.PmsProductCategoryMapper;
import com.macro.mall.mapper.PmsProductMapper;
import com.macro.mall.mapper.PmsSkuStockMapper;
import com.macro.mall.mapper.RedeemCodeRecordMapper;
import com.macro.mall.model.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.OutputStreamWriter;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RedeemService {
    @Autowired
    private RedeemCodeRecordMapper redeemCodeRecordMapper;
    @Autowired
    private PmsSkuStockMapper skuStockMapper;
    @Autowired
    private PmsProductMapper productMapper;
    @Autowired
    private PmsProductCategoryMapper productCategoryMapper;
    private static final List<String> csvHead = Arrays.asList("id","kol编码", "兑换码","使用状态", "所属游戏", "商品名称", "sku信息");


    // 可用的字符集，去掉了容易混淆的字符
    private static final String CHAR_POOL = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();


    private String generateCode(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHAR_POOL.length());
            code.append(CHAR_POOL.charAt(index));
        }
        return code.toString();
    }

    @Async
    public void generateRedeemCode(Integer generateCount, Long skuId, String kolId) {
        Assert.notNull(generateCount, "generateCount is empty");
        Assert.notNull(skuId, "skuId is empty");
        Assert.hasLength(kolId, "kolId is empty");
        Assert.state(generateCount > 0, "generateCount must be greater than 0");
        List<RedeemCodeRecord> result = new ArrayList<>();
        for (int i = 0; i < generateCount; i++) {
            RedeemCodeRecord record = new RedeemCodeRecord();
            record.setRedeemCode(generateCode(24));
            record.setSkuId(skuId);
            record.setKolId(kolId);
            record.setCreateTime(new Date());
            record.setUseStatus("NOT_USED");
            result.add(record);
        }
        for (RedeemCodeRecord redeemCodeRecord : result) {
            try {
                redeemCodeRecordMapper.insertSelective(redeemCodeRecord);

            } catch (Exception e) {
                log.error("插入失败", e);
            }
        }
    }

    public CommonPage<RedeemCodeRecord> page(RedeemSearchVO redeemSearchVO) {
        RedeemCodeRecordExample redeemCodeRecordExample = new RedeemCodeRecordExample();
        redeemCodeRecordExample.setOrderByClause("id desc");
        RedeemCodeRecordExample.Criteria criteria = redeemCodeRecordExample.createCriteria();

        if (StringUtils.isNoneEmpty(redeemSearchVO.getKolId())) {
            criteria.andKolIdEqualTo(redeemSearchVO.getKolId());
        }
        if (StringUtils.isNoneEmpty(redeemSearchVO.getUseStatus())) {
            criteria.andUseStatusEqualTo(redeemSearchVO.getUseStatus());
        }
        if (redeemSearchVO.getGameId() != null) {
            PmsProductExample productExample = new PmsProductExample();
            productExample.createCriteria().andProductCategoryIdEqualTo(redeemSearchVO.getGameId());
            List<PmsProduct> pmsProducts = productMapper.selectByExample(productExample);
            List<Long> productIds = pmsProducts.stream().map(PmsProduct::getId).toList();
            PmsSkuStockExample pmsSkuStockExample = new PmsSkuStockExample();
            pmsSkuStockExample.createCriteria().andProductIdIn(productIds);
            List<Long> list = skuStockMapper.selectByExample(pmsSkuStockExample).stream().map(PmsSkuStock::getId).toList();
            criteria.andSkuIdIn(list);
        }
        if (redeemSearchVO.getSkuId() != null) {
            criteria.andSkuIdEqualTo(redeemSearchVO.getSkuId());
        }
        Page<RedeemCodeRecord> objects = PageHelper.startPage(redeemSearchVO.getPageNum(), redeemSearchVO.getPageSize())
                .doSelectPage(() -> redeemCodeRecordMapper.selectByExample(redeemCodeRecordExample));
        return CommonPage.restPage(objects.getResult(), objects.getTotal());
    }

    public List<FullRedeemCodeRecordDTO> transfer(List<RedeemCodeRecord> list) {
        Set<Long> skuIds = list.stream().map(RedeemCodeRecord::getSkuId).collect(Collectors.toSet());
        PmsSkuStockExample skuStockExample = new PmsSkuStockExample();
        if (skuIds.isEmpty()) {
            return Collections.emptyList();
        }
        skuStockExample.createCriteria().andIdIn(new ArrayList<>(skuIds));
        Map<Long, JSONObject> collect = skuStockMapper.selectByExample(skuStockExample).parallelStream().collect(Collectors.toMap(new Function<PmsSkuStock, Long>() {
            @Override
            public Long apply(PmsSkuStock pmsSkuStock) {
                return pmsSkuStock.getId();
            }
        }, new Function<PmsSkuStock, JSONObject>() {
            @Override
            public JSONObject apply(PmsSkuStock pmsSkuStock) {
                try {
                    String spData = pmsSkuStock.getSpData();
                    JSONArray array = new JSONArray(spData);
                    List<String> infos = new ArrayList<>();
                    for (int i = 0; i < array.size(); i++) {
                        infos.add(array.getJSONObject(i).getStr("value"));
                    }
                    String skuInfo = String.join("-", infos);
                    JSONObject result = new JSONObject();
                    PmsProduct pmsProduct = productMapper.selectByPrimaryKey(pmsSkuStock.getProductId());
                    PmsProductCategory pmsProductCategory = productCategoryMapper.selectByPrimaryKey(pmsProduct.getProductCategoryId());
                    result.set("productName", pmsProduct.getName());
                    result.set("gameName", pmsProductCategory.getName());
                    result.set("skuName", skuInfo);
                    return result;
                } catch (Exception e) {
                    log.error("转化失败", e);
                    return null;
                }

            }
        }));
        return list.stream().map(new Function<RedeemCodeRecord, FullRedeemCodeRecordDTO>() {
            @Override
            @SneakyThrows
            public FullRedeemCodeRecordDTO apply(RedeemCodeRecord redeemCodeRecord) {
                FullRedeemCodeRecordDTO dto = new FullRedeemCodeRecordDTO();
                JSONObject entries = collect.get(redeemCodeRecord.getSkuId());
                if (entries != null) {
                    dto.setProductName(entries.get("productName").toString());
                    dto.setGameName(entries.get("gameName").toString());
                    dto.setSkuName(entries.get("skuName").toString());
                }

                BeanUtils.copyProperties(dto, redeemCodeRecord);
                return dto;
            }
        }).toList();
    }

    @SneakyThrows
    public void download(RedeemSearchVO queryParam, OutputStreamWriter outputStream) {
        int currentPage = 1;
        int pageSize = 100;
        String head = String.join(",", csvHead) + "\n";
        outputStream.write(head);
        queryParam.setPageNum(currentPage);
        queryParam.setPageSize(pageSize);
        while (true) {
            CommonPage<RedeemCodeRecord> list = page(queryParam);
            List<FullRedeemCodeRecordDTO> transfer = transfer(list.getList());
            for (FullRedeemCodeRecordDTO redeemCodeRecordDTO : transfer) {
                List<String> tmp = new ArrayList<>();
                tmp.add(redeemCodeRecordDTO.getId() + "");

                tmp.add(redeemCodeRecordDTO.getKolId());
                tmp.add(redeemCodeRecordDTO.getRedeemCode());
                tmp.add(redeemCodeRecordDTO.getUseStatus());
                tmp.add(redeemCodeRecordDTO.getGameName());
                tmp.add(redeemCodeRecordDTO.getProductName());
                tmp.add(redeemCodeRecordDTO.getSkuName());
                outputStream.write(String.join(",", tmp) + "\n");
            }
            if (list.getTotal() <= (long) currentPage * pageSize) {
                break;
            } else {
                currentPage++;
                queryParam.setPageNum(currentPage);
                queryParam.setPageSize(pageSize);
            }
        }
    }
}
