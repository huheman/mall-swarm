package com.macro.mall.mapper;

import com.macro.mall.model.RedeemCodeRecord;
import com.macro.mall.model.RedeemCodeRecordExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface RedeemCodeRecordMapper {
    long countByExample(RedeemCodeRecordExample example);

    int deleteByExample(RedeemCodeRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(RedeemCodeRecord row);

    int insertSelective(RedeemCodeRecord row);

    List<RedeemCodeRecord> selectByExample(RedeemCodeRecordExample example);

    RedeemCodeRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") RedeemCodeRecord row, @Param("example") RedeemCodeRecordExample example);

    int updateByExample(@Param("row") RedeemCodeRecord row, @Param("example") RedeemCodeRecordExample example);

    int updateByPrimaryKeySelective(RedeemCodeRecord row);

    int updateByPrimaryKey(RedeemCodeRecord row);
}