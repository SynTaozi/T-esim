package com.tesim.dto.admin;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AdminProductRequest {
    private String name;
    private String nameEn;
    /** 兼容管理端表单 nameZh */
    private String nameZh;
    private String description;
    private String descriptionEn;
    private String summaryZh;
    private String summaryEn;
    private String features;
    private String featuresEn;
    /** 产品说明（i18n notes） */
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
    /** 兼容管理端表单 baseValidityDays */
    private Integer baseValidityDays;
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
    /** 兼容管理端表单 isPhysical（布尔）= 是否支持实体卡 */
    private Boolean isPhysical;
    /** 是否支持 eSIM 交付 */
    private Boolean supportEsim;
    /** MONTHLY | FLAT */
    private String priceMode;
    private Integer sortOrder;
    private String spuCode;
    private String productKind;
    private Boolean isFeatured;
    private Boolean isHot;
    private List<AdminSkuRequest> skus;
}
