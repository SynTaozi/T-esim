package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.SalesOrderNotice;
import com.tesim.mapper.SalesOrderNoticeMapper;
import com.tesim.service.SalesOrderNoticeService;
import org.springframework.stereotype.Service;

@Service
public class SalesOrderNoticeServiceImpl extends ServiceImpl<SalesOrderNoticeMapper, SalesOrderNotice> implements SalesOrderNoticeService {
}
