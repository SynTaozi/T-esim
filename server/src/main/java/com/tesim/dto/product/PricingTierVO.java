package com.tesim.dto.product;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PricingTierVO {
    private Long id;
    private Long productId;
    private Integer months;
    private BigDecimal salePrice;
    /** 实体卡售价；空则与 salePrice 相同 */
    private BigDecimal physicalSalePrice;
    /** Alias of salePrice for storefront period chips. */
    private BigDecimal totalPrice;
    private BigDecimal price;
    private BigDecimal pricePerMonth;
    private BigDecimal originalPrice;
    private Integer durationDays;
    private String label;
    private Boolean isDefault;
    private Integer sortOrder;
    private Integer isActive;
}
