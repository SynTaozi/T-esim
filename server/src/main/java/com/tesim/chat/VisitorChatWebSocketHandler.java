package com.tesim.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesim.common.BizException;
import com.tesim.dto.chat.ChatSessionVO;
import com.tesim.service.ChatAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VisitorChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatAppService chatAppService;
    private final ChatRealtimeService chatRealtimeService;
    private final ChatSessionHub hub;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String token = queryParam(session, "token");
        try {
            ChatSessionVO vo = chatAppService.requireByAccessToken(token);
            session.getAttributes().put("chatToken", token);
            session.getAttributes().put("chatSessionId", vo.getId());
            hub.bindVisitor(vo.getId(), session);
            hub.send(session, ChatWsPayload.builder()
                    .type("SESSION")
                    .sessionId(vo.getId())
                    .session(vo)
                    .build());
        } catch (Exception e) {
            hub.send(session, ChatWsPayload.builder()
                    .type("ERROR")
                    .error(e.getMessage() == null ? "连接失败" : e.getMessage())
                    .build());
            try {
                session.close(CloseStatus.NOT_ACCEPTABLE);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ChatWsPayload in = objectMapper.readValue(message.getPayload(), ChatWsPayload.class);
            String type = in.getType() == null ? "" : in.getType().toUpperCase();
            if ("PING".equals(type)) {
                hub.send(session, ChatWsPayload.builder().type("PONG").build());
                return;
            }
            if (!"CHAT".equals(type)) {
                hub.send(session, ChatWsPayload.builder().type("ERROR").error("不支持的消息类型").build());
                return;
            }
            String token = String.valueOf(session.getAttributes().get("chatToken"));
            Long sessionId = (Long) session.getAttributes().get("chatSessionId");
            chatRealtimeService.visitorSendAndPush(sessionId, token, in.getContent());
        } catch (BizException e) {
            hub.send(session, ChatWsPayload.builder().type("ERROR").error(e.getMessage()).build());
        } catch (Exception e) {
            log.warn("visitor chat ws error: {}", e.getMessage());
            hub.send(session, ChatWsPayload.builder().type("ERROR").error("发送失败").build());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long sessionId = (Long) session.getAttributes().get("chatSessionId");
        if (sessionId != null) {
            hub.unbindVisitor(sessionId, session);
        }
    }

    private static String queryParam(WebSocketSession session, String key) {
        URI uri = session.getUri();
        if (uri == null || !StringUtils.hasText(uri.getQuery())) {
            return null;
        }
        for (String part : uri.getQuery().split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && key.equals(kv[0])) {
                return kv[1];
            }
        }
        return null;
    }
}
