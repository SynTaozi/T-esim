package com.tesim.dto.admin;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminCouponRequest {
    private String code;
    private BigDecimal discountAmount;
    private String useType;
    private Long categoryId;
    private BigDecimal minOrderAmount;
    private String status;
    private String startsAt;
    private String expiresAt;
    private String remark;
}
