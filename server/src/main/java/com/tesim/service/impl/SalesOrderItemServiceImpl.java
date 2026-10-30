package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.SalesOrderItem;
import com.tesim.mapper.SalesOrderItemMapper;
import com.tesim.service.SalesOrderItemService;
import org.springframework.stereotype.Service;

@Service
public class SalesOrderItemServiceImpl extends ServiceImpl<SalesOrderItemMapper, SalesOrderItem> implements SalesOrderItemService {
}
