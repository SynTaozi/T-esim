package com.tesim.dto.admin;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminSkuRequest {
    private Long id;
    private String skuCode;
    private String label;
    private String labelEn;
    private Integer months;
    private Integer durationDays;
    private BigDecimal salePrice;
    /** 实体卡售价；空则与 salePrice 相同 */
    private BigDecimal physicalSalePrice;
    private BigDecimal price;
    private BigDecimal pricePerMonth;
    private BigDecimal originalPrice;
    private Integer stock;
    private Boolean isDefault;
    private Integer status;
    private Integer sortOrder;
}
