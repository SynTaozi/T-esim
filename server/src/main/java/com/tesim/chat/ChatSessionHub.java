package com.tesim.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatSessionHub {

    private final ObjectMapper objectMapper;

    /** sessionId -> visitor WS sessions */
    private final Map<Long, Set<WebSocketSession>> visitors = new ConcurrentHashMap<>();
    /** admin WS sessions */
    private final Set<WebSocketSession> admins = new CopyOnWriteArraySet<>();

    public void bindVisitor(Long sessionId, WebSocketSession ws) {
        visitors.computeIfAbsent(sessionId, k -> new CopyOnWriteArraySet<>()).add(ws);
    }

    public void unbindVisitor(Long sessionId, WebSocketSession ws) {
        Set<WebSocketSession> set = visitors.get(sessionId);
        if (set != null) {
            set.remove(ws);
            if (set.isEmpty()) {
                visitors.remove(sessionId);
            }
        }
    }

    public void bindAdmin(WebSocketSession ws) {
        admins.add(ws);
    }

    public void unbindAdmin(WebSocketSession ws) {
        admins.remove(ws);
    }

    public void broadcastToSession(Long sessionId, ChatWsPayload payload) {
        Set<WebSocketSession> set = visitors.get(sessionId);
        if (set != null) {
            for (WebSocketSession ws : set) {
                send(ws, payload);
            }
        }
        broadcastToAdmins(payload);
    }

    public void broadcastToAdmins(ChatWsPayload payload) {
        for (WebSocketSession ws : admins) {
            send(ws, payload);
        }
    }

    public void send(WebSocketSession ws, ChatWsPayload payload) {
        if (ws == null || !ws.isOpen()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(payload);
            synchronized (ws) {
                ws.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            log.warn("WS send failed: {}", e.getMessage());
        }
    }

    public int adminOnlineCount() {
        return (int) admins.stream().filter(WebSocketSession::isOpen).count();
    }
}
