package com.tesim.service;

import com.tesim.common.PageResult;
import com.tesim.dto.chat.ChatMessageVO;
import com.tesim.dto.chat.ChatSessionVO;
import com.tesim.dto.chat.CreateChatSessionRequest;

import java.util.List;

public interface ChatAppService {

    ChatSessionVO openOrResume(CreateChatSessionRequest req);

    ChatSessionVO getByAccessToken(String accessToken);

    ChatSessionVO requireByAccessToken(String accessToken);

    List<ChatMessageVO> listMessages(Long sessionId, String accessToken);

    ChatMessageVO visitorSend(Long sessionId, String accessToken, String content);

    ChatMessageVO adminReply(Long sessionId, Long adminId, String content);

    PageResult<ChatSessionVO> adminPage(String status, Integer page, Integer size);

    ChatSessionVO adminGet(Long sessionId);

    List<ChatMessageVO> adminListMessages(Long sessionId);

    ChatSessionVO adminClose(Long sessionId);

    ChatSessionVO visitorClose(Long sessionId, String accessToken);

    void markAdminRead(Long sessionId);

    void markVisitorRead(Long sessionId, String accessToken);
}
