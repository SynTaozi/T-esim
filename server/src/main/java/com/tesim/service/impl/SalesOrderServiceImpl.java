package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.SalesOrder;
import com.tesim.mapper.SalesOrderMapper;
import com.tesim.service.SalesOrderService;
import org.springframework.stereotype.Service;

@Service
public class SalesOrderServiceImpl extends ServiceImpl<SalesOrderMapper, SalesOrder> implements SalesOrderService {
}
