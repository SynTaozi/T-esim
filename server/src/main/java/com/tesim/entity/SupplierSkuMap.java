package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * supplier_sku_map
 */
@Data
@TableName("supplier_sku_map")
public class SupplierSkuMap {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long channelId;

    private Long productId;

    private Long skuId;

    private String externalPlanId;

    private String externalName;

    private Boolean autoFulfill;

    private String extraJson;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
