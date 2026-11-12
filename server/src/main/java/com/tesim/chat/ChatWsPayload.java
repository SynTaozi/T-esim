package com.tesim.chat;

import com.tesim.dto.chat.ChatMessageVO;
import com.tesim.dto.chat.ChatSessionVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatWsPayload {
    /** CHAT / REPLY / MESSAGE / ERROR / PING / PONG / SESSION */
    private String type;
    private Long sessionId;
    private String content;
    private String error;
    private ChatMessageVO message;
    private ChatSessionVO session;
}
