package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * i18n_product
 */
@Data
@TableName("i18n_product")
public class I18nProduct {

    @TableId(value = "product_id", type = IdType.INPUT)
    private Long productId;

    private String locale;

    private String name;

    private String summary;

    private String description;

    private String features;

    private String notes;
}
