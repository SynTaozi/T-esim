package com.tesim.dto.coupon;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ValidateCouponRequest {
    private String code;
    private BigDecimal orderAmount;
    private Long productId;
}
