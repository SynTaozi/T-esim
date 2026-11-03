package com.tesim.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductVO {
    private Long id;
    private String name;
    private String nameZh;
    private String nameEn;
    private String description;
    private String descriptionEn;
    private String summaryZh;
    private String summaryEn;
    private String features;
    private String featuresEn;
    private String notesZh;
    private String notesEn;
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
    /** 1=ON_SALE, 0=OFF_SALE */
    private Integer status;
    private Integer productType;
    private Integer itemType;
    private Long categoryId;
    private String categoryName;
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
    private List<AdminSkuVO> skus;
    private List<AdminSkuVO> pricingTiers;
}
