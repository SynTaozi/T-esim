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
 * member_subscription
 */
@Data
@TableName("member_subscription")
public class MemberSubscription {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    private Long orderId;

    private Long orderItemId;

    private Long productId;

    private Long skuId;

    private String msisdn;

    private String countryCode;

    private String carrier;

    private String planSnapshot;

    private BigDecimal purchasePrice;

    private BigDecimal renewalPrice;

    private String status;

    private LocalDateTime activatedAt;

    private LocalDateTime expiresAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
