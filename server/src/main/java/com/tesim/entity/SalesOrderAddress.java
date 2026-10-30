package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * sales_order_address
 */
@Data
@TableName("sales_order_address")
public class SalesOrderAddress {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String addressType;

    private String receiverName;

    private String phone;

    private String email;

    private String country;

    private String province;

    private String city;

    private String addressLine;

    private String postalCode;
}
