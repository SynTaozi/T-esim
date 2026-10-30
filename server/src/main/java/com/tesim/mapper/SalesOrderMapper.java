package com.tesim.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tesim.entity.SalesOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SalesOrderMapper extends BaseMapper<SalesOrder> {
}
