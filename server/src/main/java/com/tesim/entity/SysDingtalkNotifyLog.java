package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * sys_dingtalk_notify_log
 */
@Data
@TableName("sys_dingtalk_notify_log")
public class SysDingtalkNotifyLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long configId;

    private String eventType;

    private String title;

    private String content;

    private String status;

    private String errorMessage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
