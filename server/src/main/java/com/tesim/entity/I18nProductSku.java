package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * i18n_product_sku
 */
@Data
@TableName("i18n_product_sku")
public class I18nProductSku {

    @TableId(value = "sku_id", type = IdType.INPUT)
    private Long skuId;

    private String locale;

    private String label;
}
