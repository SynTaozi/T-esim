package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * i18n_cms_faq
 */
@Data
@TableName("i18n_cms_faq")
public class I18nCmsFaq {

    @TableId(value = "faq_id", type = IdType.INPUT)
    private Long faqId;

    private String locale;

    private String question;

    private String answer;
}
