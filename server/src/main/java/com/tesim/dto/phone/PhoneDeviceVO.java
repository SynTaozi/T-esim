package com.tesim.dto.phone;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PhoneDeviceVO {
    private Long id;
    private String name;
    private String nameEn;
    private String description;
    private String descriptionEn;
    private BigDecimal price;
    private Boolean isFree;
    private String deviceType;
    private BigDecimal shippingFee;
}
