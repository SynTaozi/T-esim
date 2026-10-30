package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * sys_email_account
 */
@Data
@TableName("sys_email_account")
public class SysEmailAccount {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String emailAddress;

    private String username;

    private String password;

    private String smtpHost;

    private Integer smtpPort;

    private String imapHost;

    private Integer imapPort;

    private String purpose;

    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
