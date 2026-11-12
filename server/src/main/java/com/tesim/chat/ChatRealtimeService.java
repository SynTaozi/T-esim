package com.tesim.chat;

import com.tesim.dto.chat.ChatMessageVO;
import com.tesim.dto.chat.ChatSessionVO;
import com.tesim.service.ChatAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRealtimeService {

    private final ChatAppService chatAppService;
    private final ChatSessionHub hub;

    public ChatMessageVO visitorSendAndPush(Long sessionId, String accessToken, String content) {
        ChatMessageVO msg = chatAppService.visitorSend(sessionId, accessToken, content);
        ChatSessionVO session = chatAppService.adminGet(sessionId);
        ChatWsPayload payload = ChatWsPayload.builder()
                .type("MESSAGE")
                .sessionId(sessionId)
                .message(msg)
                .session(session)
                .build();
        hub.broadcastToSession(sessionId, payload);
        return msg;
    }

    public ChatMessageVO adminReplyAndPush(Long sessionId, Long adminId, String content) {
        ChatMessageVO msg = chatAppService.adminReply(sessionId, adminId, content);
        ChatSessionVO session = chatAppService.adminGet(sessionId);
        ChatWsPayload payload = ChatWsPayload.builder()
                .type("MESSAGE")
                .sessionId(sessionId)
                .message(msg)
                .session(session)
                .build();
        hub.broadcastToSession(sessionId, payload);
        return msg;
    }
}
