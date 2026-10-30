package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * product_coverage
 */
@Data
@TableName("product_coverage")
public class ProductCoverage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private String countryCode;

    private String regionCode;
}
