package com.tesim.dto.ticket;

import lombok.Data;

import java.util.List;

@Data
public class TicketReplyRequest {
    private String content;
    /** 已上传图片 URL（先调 /files/upload） */
    private List<String> imageUrls;
}
