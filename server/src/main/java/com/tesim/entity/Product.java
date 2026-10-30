package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * product
 */
@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long categoryId;

    private String spuCode;

    private String productKind;

    private Integer planMode;

    private Integer topupMode;

    private String dataLimit;

    private Integer baseValidityDays;

    private Boolean supportSms;

    private Boolean supportData;

    private Boolean supportVoice;

    private Boolean isPhysical;

    private BigDecimal costPrice;

    private BigDecimal physicalCost;

    private String imageUrl;

    private String displayImageUrl;

    private String status;

    private Boolean isFeatured;

    private Boolean isHot;

    private Integer sortOrder;

    private String supplierFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private Boolean deleted;

    @TableField(value = "spu_code_active", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String spuCodeActive;
}
