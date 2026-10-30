package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * member_address
 */
@Data
@TableName("member_address")
public class MemberAddress {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    private String receiverName;

    private String phone;

    private String email;

    private String country;

    private String province;

    private String city;

    private String addressLine;

    private String postalCode;

    private Boolean isDefault;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
