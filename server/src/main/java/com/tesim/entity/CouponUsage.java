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
 * coupon_usage
 */
@Data
@TableName("coupon_usage")
public class CouponUsage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long couponId;

    private String couponCode;

    private Long orderId;

    private String orderNo;

    private Long memberId;

    private BigDecimal discountAmount;

    private String status;

    private LocalDateTime usedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
