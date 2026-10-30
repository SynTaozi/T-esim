package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * sales_order_shipment
 */
@Data
@TableName("sales_order_shipment")
public class SalesOrderShipment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String shipStatus;

    private String carrierCode;

    private String trackingNo;

    private String shippingPhone;

    private String shippingRemark;

    private LocalDateTime shippedAt;

    private Boolean emailSent;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
