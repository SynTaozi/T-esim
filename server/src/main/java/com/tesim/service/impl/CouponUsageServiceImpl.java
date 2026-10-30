package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.CouponUsage;
import com.tesim.mapper.CouponUsageMapper;
import com.tesim.service.CouponUsageService;
import org.springframework.stereotype.Service;

@Service
public class CouponUsageServiceImpl extends ServiceImpl<CouponUsageMapper, CouponUsage> implements CouponUsageService {
}
