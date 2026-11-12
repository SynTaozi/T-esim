package com.tesim.controller;

import com.tesim.common.ApiResponse;
import com.tesim.dto.chat.ChatMessageVO;
import com.tesim.dto.chat.ChatSessionVO;
import com.tesim.dto.chat.CreateChatSessionRequest;
import com.tesim.chat.ChatRealtimeService;
import com.tesim.service.ChatAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatAppService chatAppService;
    private final ChatRealtimeService chatRealtimeService;

    @PostMapping("/sessions")
    public ApiResponse<ChatSessionVO> open(@RequestBody CreateChatSessionRequest req) {
        return ApiResponse.ok(chatAppService.openOrResume(req));
    }

    @GetMapping("/sessions/{id}/messages")
    public ApiResponse<List<ChatMessageVO>> messages(
            @PathVariable Long id,
            @RequestHeader("X-Chat-Token") String token) {
        return ApiResponse.ok(chatAppService.listMessages(id, token));
    }

    @PostMapping("/sessions/{id}/messages")
    public ApiResponse<ChatMessageVO> send(
            @PathVariable Long id,
            @RequestHeader("X-Chat-Token") String token,
            @RequestBody Map<String, String> body) {
        return ApiResponse.ok(chatRealtimeService.visitorSendAndPush(id, token, body == null ? null : body.get("content")));
    }

    @PostMapping("/sessions/{id}/read")
    public ApiResponse<Void> read(
            @PathVariable Long id,
            @RequestHeader("X-Chat-Token") String token) {
        chatAppService.markVisitorRead(id, token);
        return ApiResponse.ok(null);
    }

    @PostMapping("/sessions/{id}/close")
    public ApiResponse<ChatSessionVO> close(
            @PathVariable Long id,
            @RequestHeader("X-Chat-Token") String token) {
        return ApiResponse.ok(chatAppService.visitorClose(id, token));
    }

    @GetMapping("/sessions/me")
    public ApiResponse<ChatSessionVO> me(@RequestParam String token) {
        return ApiResponse.ok(chatAppService.requireByAccessToken(token));
    }
}
