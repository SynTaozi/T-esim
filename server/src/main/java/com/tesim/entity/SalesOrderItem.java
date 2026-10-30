package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * sales_order_item
 */
@Data
@TableName("sales_order_item")
public class SalesOrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long productId;

    private Long skuId;

    private String productKind;

    private String skuCode;

    private String titleZh;

    private String titleEn;

    private Integer months;

    private Integer validityDays;

    private BigDecimal renewalPrice;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal pricePerMonth;

    private BigDecimal lineAmount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
