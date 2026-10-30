package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * sys_locale
 */
@Data
@TableName("sys_locale")
public class SysLocale {

    @TableId(value = "code", type = IdType.INPUT)
    private String code;

    private String name;

    private Boolean isDefault;

    private Integer sortOrder;

    private Boolean enabled;
}
