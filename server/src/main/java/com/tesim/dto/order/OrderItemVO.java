package com.tesim.dto.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemVO {
    private Long id;
    private Long productId;
    private String productName;
    private String nameEn;
    private String nameZh;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private String itemType;
    private String dataLimit;
    private Integer validityDays;
    private Integer months;
    private String applicableRegions;
    private String countries;
    private String productType;
}
