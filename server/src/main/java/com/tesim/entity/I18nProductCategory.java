package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * i18n_product_category
 */
@Data
@TableName("i18n_product_category")
public class I18nProductCategory {

    @TableId(value = "category_id", type = IdType.INPUT)
    private Long categoryId;

    private String locale;

    private String name;

    private String description;
}
