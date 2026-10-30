package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.SalesOrderEsim;
import com.tesim.mapper.SalesOrderEsimMapper;
import com.tesim.service.SalesOrderEsimService;
import org.springframework.stereotype.Service;

@Service
public class SalesOrderEsimServiceImpl extends ServiceImpl<SalesOrderEsimMapper, SalesOrderEsim> implements SalesOrderEsimService {
}
