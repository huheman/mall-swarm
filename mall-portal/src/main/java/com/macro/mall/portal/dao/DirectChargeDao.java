package com.macro.mall.portal.dao;

import com.macro.mall.portal.domain.DirectChargeDomain;
import org.apache.ibatis.annotations.Param;

public interface DirectChargeDao {

    DirectChargeDomain selectByOrderSN(@Param("orderSN")String orderSN);

    int insert(DirectChargeDomain chargeDomain);

    void update(DirectChargeDomain chargeDomain);

    DirectChargeDomain selectById(long id);
}
