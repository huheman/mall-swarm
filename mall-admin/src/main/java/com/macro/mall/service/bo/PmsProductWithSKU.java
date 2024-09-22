package com.macro.mall.service.bo;

import com.macro.mall.model.PmsProduct;
import com.macro.mall.model.PmsSkuStock;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class PmsProductWithSKU extends PmsProduct {
    private List<PmsSkuStock> skuInfo;
    
}