package com.tesim.dto.admin;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AdminProductSaveRequest {
    private Long categoryId;
    private String spuCode;
    private String productKind;
    private String nameZh;
    private String nameEn;
    private String summaryZh;
    private String summaryEn;
    private String descriptionZh;
    private String descriptionEn;
    private String imageUrl;
    private String displayImageUrl;
    private String status;
    private Boolean isFeatured;
    private Boolean isHot;
    private Boolean isPhysical;
    private Integer sortOrder;
    private String dataLimit;
    private Integer baseValidityDays;
    private BigDecimal costPrice;
    private String supplierFlag;
    private List<String> coverageRegions;
    private List<AdminSkuVO> skus;
}
