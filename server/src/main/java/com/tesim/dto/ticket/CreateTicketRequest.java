package com.tesim.dto.ticket;

import lombok.Data;

import java.util.List;

@Data
public class CreateTicketRequest {
    private String type;
    private String priority;
    private Long orderId;
    private String subject;
    private String content;
    /** 已上传图片的 URL 列表（先调 /files/upload） */
    private List<String> imageUrls;
}
