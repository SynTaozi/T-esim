package com.tesim.dto.phone;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UserPhoneVO {
    private Long id;
    private String phoneNumber;
    private String countryCode;
    private String country;
    private String carrier;
    private BigDecimal purchasePrice;
    private BigDecimal renewalPrice;
    private String expiryDate;
    private String status;
    private String notes;
    private String createdAt;
    private String updatedAt;
}
