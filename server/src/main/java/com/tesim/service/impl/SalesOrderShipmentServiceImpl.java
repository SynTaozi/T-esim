package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.SalesOrderShipment;
import com.tesim.mapper.SalesOrderShipmentMapper;
import com.tesim.service.SalesOrderShipmentService;
import org.springframework.stereotype.Service;

@Service
public class SalesOrderShipmentServiceImpl extends ServiceImpl<SalesOrderShipmentMapper, SalesOrderShipment> implements SalesOrderShipmentService {
}
