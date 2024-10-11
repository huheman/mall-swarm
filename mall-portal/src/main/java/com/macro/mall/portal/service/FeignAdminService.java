package com.macro.mall.portal.service;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.portal.service.bo.OmsOrderDeliveryParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("mall-admin")
public interface FeignAdminService {

    @PostMapping(value = "/order/update/delivery")
    CommonResult delivery(@RequestBody List<OmsOrderDeliveryParam> deliveryParamList);

    @PostMapping("/aliyun/oss/upload")
    @ResponseBody
    CommonResult<String> upload(@RequestBody byte[] file);
}
