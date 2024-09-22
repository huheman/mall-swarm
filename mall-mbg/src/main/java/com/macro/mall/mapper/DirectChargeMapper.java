package com.macro.mall.mapper;

import com.macro.mall.model.DirectCharge;
import com.macro.mall.model.DirectChargeExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DirectChargeMapper {
    long countByExample(DirectChargeExample example);

    int deleteByExample(DirectChargeExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DirectCharge row);

    int insertSelective(DirectCharge row);

    List<DirectCharge> selectByExample(DirectChargeExample example);

    DirectCharge selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") DirectCharge row, @Param("example") DirectChargeExample example);

    int updateByExample(@Param("row") DirectCharge row, @Param("example") DirectChargeExample example);

    int updateByPrimaryKeySelective(DirectCharge row);

    int updateByPrimaryKey(DirectCharge row);
}