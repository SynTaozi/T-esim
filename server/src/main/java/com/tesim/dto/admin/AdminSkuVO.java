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
public class AdminSkuVO {
    private Long id;
    private Long productId;
    private String skuCode;
    private String label;
    private String labelEn;
    private Integer months;
    private Integer durationDays;
    private BigDecimal salePrice;
    private BigDecimal price;
    private BigDecimal pricePerMonth;
    private BigDecimal originalPrice;
    private Integer stock;
    private Boolean isDefault;
    private Integer status;
    private Integer sortOrder;
}
