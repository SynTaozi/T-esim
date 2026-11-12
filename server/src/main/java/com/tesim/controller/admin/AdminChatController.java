package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.common.PageResult;
import com.tesim.dto.chat.ChatMessageVO;
import com.tesim.dto.chat.ChatSessionVO;
import com.tesim.chat.ChatRealtimeService;
import com.tesim.chat.ChatSessionHub;
import com.tesim.chat.ChatWsPayload;
import com.tesim.security.SecurityUtils;
import com.tesim.service.ChatAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/chat")
@RequiredArgsConstructor
public class AdminChatController {

    private final ChatAppService chatAppService;
    private final ChatRealtimeService chatRealtimeService;
    private final ChatSessionHub chatSessionHub;

    @GetMapping("/sessions")
    public ApiResponse<PageResult<ChatSessionVO>> page(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok(chatAppService.adminPage(status, page, size));
    }

    @GetMapping("/sessions/{id}")
    public ApiResponse<ChatSessionVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(chatAppService.adminGet(id));
    }

    @GetMapping("/sessions/{id}/messages")
    public ApiResponse<List<ChatMessageVO>> messages(@PathVariable Long id) {
        chatAppService.markAdminRead(id);
        return ApiResponse.ok(chatAppService.adminListMessages(id));
    }

    @PostMapping("/sessions/{id}/messages")
    public ApiResponse<ChatMessageVO> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long adminId = SecurityUtils.memberIdOrNull();
        return ApiResponse.ok(chatRealtimeService.adminReplyAndPush(id, adminId, body == null ? null : body.get("content")));
    }

    @PostMapping("/sessions/{id}/close")
    public ApiResponse<ChatSessionVO> close(@PathVariable Long id) {
        ChatSessionVO closed = chatAppService.adminClose(id);
        chatSessionHub.broadcastToSession(id, ChatWsPayload.builder()
                .type("SESSION")
                .sessionId(id)
                .session(closed)
                .build());
        return ApiResponse.ok(closed);
    }

    @PostMapping("/sessions/{id}/read")
    public ApiResponse<Void> read(@PathVariable Long id) {
        chatAppService.markAdminRead(id);
        return ApiResponse.ok(null);
    }
}
