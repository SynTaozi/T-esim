package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * i18n_sys_email_template
 */
@Data
@TableName("i18n_sys_email_template")
public class I18nSysEmailTemplate {

    @TableId(value = "template_id", type = IdType.INPUT)
    private Long templateId;

    private String locale;

    private String name;

    private String subject;

    private String body;
}
