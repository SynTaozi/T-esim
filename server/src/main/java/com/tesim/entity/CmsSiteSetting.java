package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * cms_site_setting
 */
@Data
@TableName("cms_site_setting")
public class CmsSiteSetting {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String loginBannerUrl;

    private String registerBannerUrl;

    private String homeHeroImageUrl;

    private BigDecimal defaultShippingFee;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
