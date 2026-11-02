package com.tesim.dto.coupon;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ValidateCouponVO {
    private Boolean valid;
    private String code;
    private Long couponId;
    private BigDecimal discountAmount;
    private String message;
}
