package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.Coupon;
import com.tesim.mapper.CouponMapper;
import com.tesim.service.CouponService;
import org.springframework.stereotype.Service;

@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {
}
