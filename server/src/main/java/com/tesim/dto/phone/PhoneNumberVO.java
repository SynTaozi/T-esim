package com.tesim.dto.phone;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PhoneNumberVO {
    private Long id;
    private String name;
    private String nameEn;
    private String countryCode;
    private String country;
    private String countryEn;
    private String region;
    private String operator;
    private List<String> features;
    private String description;
    private String numberType;
    private List<String> tips;
    private List<String> warnings;
    private BigDecimal monthlyPrice;
    private BigDecimal maintenancePrice;
    private BigDecimal originalPrice;
    private Integer stock;
    private String whatsappTip;
    private Integer status;
}
