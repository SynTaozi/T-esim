package com.tesim.dto.phone;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PhoneStatsVO {
    private long totalCount;
    private long activeCount;
    private long expiredCount;
    private BigDecimal totalSpent;
    private long expiringCount;
    private List<UserPhoneVO> expiringNumbers;
}
