package com.tesim.dto.chat;

import lombok.Data;

@Data
public class CreateChatSessionRequest {
    /** 浏览器本地生成的访客 ID */
    private String visitorId;
    private String nickname;
    private String email;
    /** 可选：已登录会员 ID（前端有 token 时可传，服务端也会尝试从 SecurityContext 取） */
    private Long memberId;
}
