package com.macro.mall.mapper;

import com.macro.mall.model.SmsKolPromoConfig;
import com.macro.mall.model.SmsKolPromoConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SmsKolPromoConfigMapper {
    long countByExample(SmsKolPromoConfigExample example);

    int deleteByExample(SmsKolPromoConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SmsKolPromoConfig row);

    int insertSelective(SmsKolPromoConfig row);

    List<SmsKolPromoConfig> selectByExample(SmsKolPromoConfigExample example);

    SmsKolPromoConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") SmsKolPromoConfig row, @Param("example") SmsKolPromoConfigExample example);

    int updateByExample(@Param("row") SmsKolPromoConfig row, @Param("example") SmsKolPromoConfigExample example);

    int updateByPrimaryKeySelective(SmsKolPromoConfig row);

    int updateByPrimaryKey(SmsKolPromoConfig row);
}