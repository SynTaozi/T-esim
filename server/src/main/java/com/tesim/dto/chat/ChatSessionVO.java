package com.tesim.dto.chat;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatSessionVO {
    private Long id;
    private String sessionNo;
    private String accessToken;
    private String visitorId;
    private Long memberId;
    private String nickname;
    private String email;
    private String status;
    private String lastMessageAt;
    private String lastPreview;
    private Integer unreadAdmin;
    private Integer unreadVisitor;
    private String createdAt;
    private List<ChatMessageVO> messages;
}
