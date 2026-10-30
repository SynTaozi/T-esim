package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * i18n_cms_app_download
 */
@Data
@TableName("i18n_cms_app_download")
public class I18nCmsAppDownload {

    @TableId(value = "download_id", type = IdType.INPUT)
    private Long downloadId;

    private String locale;

    private String name;

    private String remark;
}
