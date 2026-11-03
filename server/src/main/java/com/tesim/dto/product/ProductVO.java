package com.tesim.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductVO {
    private Long id;
    private String name;
    private String nameEn;
    private String description;
    private String descriptionEn;
    private String summary;
    private String summaryEn;
    private String features;
    private String featuresEn;
    private String notes;
    private String notesEn;
    /** 兼容旧前台字段名（产品说明） */
    private String importantLabels;
    private String importantLabelsEn;
    private BigDecimal price;
    private String dataLimit;
    private Integer validityDays;
    private Integer stock;
    private String imageUrl;
    private String displayImageUrl;
    private Integer status;
    @JsonProperty("planType")
    private Integer planType;
    @JsonProperty("topupType")
    private Integer topupType;
    private Integer smsSupport;
    private Integer dataSupport;
    private Integer voiceSupport;
    private Integer isPhysicalCard;
    private Boolean isFeatured;
    private Boolean isHot;
    private Long itemType;
    private String categoryName;
    private String categoryCode;
    private String countries;
    private String applicableRegions;
    private Integer sortOrder;
    private String productKind;
    private String spuCode;
}
