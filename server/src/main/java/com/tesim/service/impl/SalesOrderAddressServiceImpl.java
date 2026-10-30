package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.SalesOrderAddress;
import com.tesim.mapper.SalesOrderAddressMapper;
import com.tesim.service.SalesOrderAddressService;
import org.springframework.stereotype.Service;

@Service
public class SalesOrderAddressServiceImpl extends ServiceImpl<SalesOrderAddressMapper, SalesOrderAddress> implements SalesOrderAddressService {
}
