package com.tesim.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCouponVO {
    private Long id;
    private String code;
    private BigDecimal discountAmount;
    private String useType;
    private Long categoryId;
    private String status;
    private BigDecimal minOrderAmount;
    private String startsAt;
    private String expiresAt;
    private String remark;
    private long usedCount;
}
