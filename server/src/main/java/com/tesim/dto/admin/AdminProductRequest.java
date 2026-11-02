package com.tesim.dto.admin;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AdminProductRequest {
    private String name;
    private String nameEn;
    private String description;
    private String descriptionEn;
    private String features;
    private String featuresEn;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal physicalCost;
    private BigDecimal costPrice;
    private List<String> applicableRegions;
    private String countries;
    private String dataLimit;
    private Integer validityDays;
    private Integer stock;
    private String imageUrl;
    private String displayImageUrl;
    private Integer status;
    private Integer productType;
    private Integer itemType;
    private Long categoryId;
    private Long subCategoryId;
    private Integer planType;
    private Integer smsSupport;
    private Integer dataSupport;
    private Integer voiceSupport;
    private Integer topupType;
    private Integer isPhysicalCard;
    private Integer sortOrder;
    private String spuCode;
    private String productKind;
    private Boolean isFeatured;
    private Boolean isHot;
    private List<AdminSkuRequest> skus;
}
