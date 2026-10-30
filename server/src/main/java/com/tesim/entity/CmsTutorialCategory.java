package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * cms_tutorial_category
 */
@Data
@TableName("cms_tutorial_category")
public class CmsTutorialCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String icon;

    private Integer sortOrder;

    private Integer status;
}
