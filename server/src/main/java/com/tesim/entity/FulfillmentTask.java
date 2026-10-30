package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * fulfillment_task
 */
@Data
@TableName("fulfillment_task")
public class FulfillmentTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long channelId;

    private String taskStatus;

    private Integer retryCount;

    private LocalDateTime nextRetryAt;

    private String externalRef;

    private String errorMessage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
