package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * i18n_payment_channel
 */
@Data
@TableName("i18n_payment_channel")
public class I18nPaymentChannel {

    @TableId(value = "channel_id", type = IdType.INPUT)
    private Long channelId;

    private String locale;

    private String name;

    private String description;
}
