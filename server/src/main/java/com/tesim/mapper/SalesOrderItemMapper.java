package com.tesim.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tesim.entity.SalesOrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SalesOrderItemMapper extends BaseMapper<SalesOrderItem> {
}
