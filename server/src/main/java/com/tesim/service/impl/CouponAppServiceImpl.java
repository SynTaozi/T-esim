package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.CouponAppService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tesim.dto.coupon.ValidateCouponRequest;
import com.tesim.dto.coupon.ValidateCouponVO;
import com.tesim.entity.Coupon;
import com.tesim.entity.CouponUsage;
import com.tesim.entity.SalesOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponAppServiceImpl implements CouponAppService {

    private final CouponService couponService;
    private final CouponUsageService couponUsageService;

    public ValidateCouponVO validate(String code, BigDecimal orderAmount, Long productId) {
        if (!StringUtils.hasText(code)) {
            return ValidateCouponVO.builder()
                    .valid(false)
                    .code(code)
                    .message("请输入优惠券")
                    .build();
        }
        Coupon coupon = couponService.getOne(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getCode, code.trim().toUpperCase())
                .last("LIMIT 1"));
        if (coupon == null) {
            coupon = couponService.getOne(new LambdaQueryWrapper<Coupon>()
                    .eq(Coupon::getCode, code.trim())
                    .last("LIMIT 1"));
        }
        if (coupon == null) {
            return ValidateCouponVO.builder()
                    .valid(false)
                    .code(code)
                    .message("优惠券不存在")
                    .build();
        }
        if (!"ENABLED".equalsIgnoreCase(coupon.getStatus()) && !"ACTIVE".equalsIgnoreCase(coupon.getStatus())) {
            return ValidateCouponVO.builder()
                    .valid(false)
                    .code(code)
                    .message("优惠券已停用")
                    .build();
        }
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartsAt() != null && now.isBefore(coupon.getStartsAt())) {
            return ValidateCouponVO.builder()
                    .valid(false)
                    .code(code)
                    .message("优惠券尚未生效")
                    .build();
        }
        if (coupon.getExpiresAt() != null && now.isAfter(coupon.getExpiresAt())) {
            return ValidateCouponVO.builder()
                    .valid(false)
                    .code(code)
                    .message("优惠券已过期")
                    .build();
        }
        BigDecimal amount = orderAmount == null ? BigDecimal.ZERO : orderAmount;
        if (coupon.getMinOrderAmount() != null && amount.compareTo(coupon.getMinOrderAmount()) < 0) {
            return ValidateCouponVO.builder()
                    .valid(false)
                    .code(code)
                    .message("未满足最低消费金额")
                    .build();
        }
        if (coupon.getCategoryId() != null && productId != null) {
            // category restriction checked loosely; detailed product category match left for later
        }
        BigDecimal discount = coupon.getDiscountAmount() == null ? BigDecimal.ZERO : coupon.getDiscountAmount();
        if (discount.compareTo(amount) > 0) {
            discount = amount;
        }
        return ValidateCouponVO.builder()
                .valid(true)
                .couponId(coupon.getId())
                .code(coupon.getCode())
                .discountAmount(discount)
                .message("优惠券可用")
                .build();
    }

    public ValidateCouponVO validateRequest(ValidateCouponRequest req) {
        return validate(
                req.getCode(),
                req.getOrderAmount() != null ? req.getOrderAmount() : BigDecimal.ZERO,
                req.getProductId()
        );
    }

    public void reserve(Long couponId, String code, SalesOrder order, Long memberId, BigDecimal discount) {
        CouponUsage usage = new CouponUsage();
        usage.setCouponId(couponId);
        usage.setCouponCode(code);
        usage.setOrderId(order.getId());
        usage.setOrderNo(order.getOrderNo());
        usage.setMemberId(memberId);
        usage.setDiscountAmount(discount);
        usage.setStatus("RESERVED");
        usage.setUsedAt(LocalDateTime.now());
        couponUsageService.save(usage);
    }
}
