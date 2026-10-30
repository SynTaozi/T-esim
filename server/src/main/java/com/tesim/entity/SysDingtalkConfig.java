package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * sys_dingtalk_config
 */
@Data
@TableName("sys_dingtalk_config")
public class SysDingtalkConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String webhookUrl;

    private Boolean enabled;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
