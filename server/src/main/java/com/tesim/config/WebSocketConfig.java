package com.tesim.config;

import com.tesim.chat.AdminChatWebSocketHandler;
import com.tesim.chat.VisitorChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final VisitorChatWebSocketHandler visitorChatWebSocketHandler;
    private final AdminChatWebSocketHandler adminChatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(visitorChatWebSocketHandler, "/ws/chat")
                .setAllowedOriginPatterns("*");
        registry.addHandler(adminChatWebSocketHandler, "/ws/admin/chat")
                .setAllowedOriginPatterns("*");
    }
}
