package com.macro.mall.portal.service.bo;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductSkuBO {
    private Long productId;
    private String pic;
    private String subTitle;
    private String brandName;
    private String name;
    private List<SkuBO> skuStockList;


    @Data
    public static class SkuBO {
        private Long productId;
        private String skuCode;
        private BigDecimal price;
        private Long id;
        private BigDecimal promotionPrice;
        private String spData;
    }
}
