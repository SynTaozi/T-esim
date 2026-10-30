package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * sales_order_notice
 */
@Data
@TableName("sales_order_notice")
public class SalesOrderNotice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String noticeType;

    private String subject;

    private String content;

    private LocalDateTime sentAt;
}
