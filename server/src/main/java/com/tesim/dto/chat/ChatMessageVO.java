package com.tesim.dto.chat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMessageVO {
    private Long id;
    private Long sessionId;
    private String senderType;
    private String content;
    private String createdAt;
}
