package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * i18n_cms_tutorial
 */
@Data
@TableName("i18n_cms_tutorial")
public class I18nCmsTutorial {

    @TableId(value = "tutorial_id", type = IdType.INPUT)
    private Long tutorialId;

    private String locale;

    private String title;

    private String summary;

    private String content;
}
