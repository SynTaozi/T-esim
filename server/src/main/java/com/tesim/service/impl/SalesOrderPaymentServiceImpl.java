package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.SalesOrderPayment;
import com.tesim.mapper.SalesOrderPaymentMapper;
import com.tesim.service.SalesOrderPaymentService;
import org.springframework.stereotype.Service;

@Service
public class SalesOrderPaymentServiceImpl extends ServiceImpl<SalesOrderPaymentMapper, SalesOrderPayment> implements SalesOrderPaymentService {
}
