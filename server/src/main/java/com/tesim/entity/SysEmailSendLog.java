package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_email_send_log")
public class SysEmailSendLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long accountId;

    private String purpose;

    private String bizType;

    private String bizId;

    private String toEmail;

    private String subject;

    private String body;

    /** PENDING / SUCCESS / FAILED */
    private String status;

    private String errorMessage;

    private Integer attemptCount;

    private LocalDateTime sentAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
