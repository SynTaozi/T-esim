package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * supplier_channel
 */
@Data
@TableName("supplier_channel")
public class SupplierChannel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private String environment;

    private String baseUrl;

    private String credentialJson;

    private String callbackUrl;

    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
