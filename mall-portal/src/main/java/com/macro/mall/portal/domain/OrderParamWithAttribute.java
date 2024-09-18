package com.macro.mall.portal.domain;

import com.macro.mall.portal.service.bo.CartAttributeBO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 生成订单时传入的参数
 * Created by macro on 2018/8/30.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OrderParamWithAttribute extends OrderParam {
    @Schema(title = "用户勾选的属性")
    private List<CartAttributeBO> attributeBOS;
}
