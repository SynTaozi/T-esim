package com.tesim.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesim.common.BizException;
import com.tesim.security.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatRealtimeService chatRealtimeService;
    private final ChatSessionHub hub;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            String token = queryParam(session, "token");
            if (!StringUtils.hasText(token)) {
                throw new BizException(401, "缺少登录凭证");
            }
            Claims claims = jwtUtil.parse(token);
            String role = String.valueOf(claims.getOrDefault("role", "USER")).trim().toUpperCase();
            if (role.startsWith("ROLE_")) {
                role = role.substring(5);
            }
            if (!"ADMIN".equals(role)) {
                throw new BizException(403, "无权限");
            }
            Long adminId = Long.valueOf(claims.getSubject());
            session.getAttributes().put("adminId", adminId);
            hub.bindAdmin(session);
            hub.send(session, ChatWsPayload.builder().type("PONG").build());
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
            if (!"REPLY".equals(type)) {
                hub.send(session, ChatWsPayload.builder().type("ERROR").error("不支持的消息类型").build());
                return;
            }
            if (in.getSessionId() == null) {
                hub.send(session, ChatWsPayload.builder().type("ERROR").error("缺少会话 ID").build());
                return;
            }
            Long adminId = (Long) session.getAttributes().get("adminId");
            chatRealtimeService.adminReplyAndPush(in.getSessionId(), adminId, in.getContent());
        } catch (BizException e) {
            hub.send(session, ChatWsPayload.builder().type("ERROR").error(e.getMessage()).build());
        } catch (Exception e) {
            log.warn("admin chat ws error: {}", e.getMessage());
            hub.send(session, ChatWsPayload.builder().type("ERROR").error("发送失败").build());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        hub.unbindAdmin(session);
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
