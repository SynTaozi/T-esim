package com.tesim.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * ticket_message
 */
@Data
@TableName("ticket_message")
public class TicketMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ticketId;

    private String senderType;

    private Long senderId;

    private String senderName;

    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
