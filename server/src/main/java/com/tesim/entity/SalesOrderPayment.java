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
 * sales_order_payment
 */
@Data
@TableName("sales_order_payment")
public class SalesOrderPayment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String channelCode;

    private String payStatus;

    private BigDecimal amount;

    private String currency;

    private String thirdPartyTradeNo;

    private String transactionId;

    private BigDecimal cryptoAmount;

    private String cryptoAddress;

    private LocalDateTime expireAt;

    private LocalDateTime paidAt;

    private String notifyPayload;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
