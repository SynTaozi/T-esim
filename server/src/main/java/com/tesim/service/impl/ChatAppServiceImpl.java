package com.tesim.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.chat.ChatMessageVO;
import com.tesim.dto.chat.ChatSessionVO;
import com.tesim.dto.chat.CreateChatSessionRequest;
import com.tesim.entity.ChatMessage;
import com.tesim.entity.ChatSession;
import com.tesim.security.SecurityUtils;
import com.tesim.service.ChatAppService;
import com.tesim.service.ChatMessageService;
import com.tesim.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatAppServiceImpl implements ChatAppService {

    public static final int MAX_CONTENT_LEN = 1000;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;

    @Override
    @Transactional
    public ChatSessionVO openOrResume(CreateChatSessionRequest req) {
        if (req == null || !StringUtils.hasText(req.getVisitorId())) {
            throw new BizException("缺少访客标识");
        }
        String visitorId = req.getVisitorId().trim();
        if (visitorId.length() > 64) {
            throw new BizException("访客标识无效");
        }

        Long memberId = SecurityUtils.memberIdOrNull();
        if (memberId == null && req.getMemberId() != null) {
            memberId = req.getMemberId();
        }

        ChatSession open = chatSessionService.getOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getVisitorId, visitorId)
                .eq(ChatSession::getStatus, "OPEN")
                .orderByDesc(ChatSession::getUpdatedAt)
                .last("LIMIT 1"));

        if (open != null) {
            boolean dirty = false;
            if (StringUtils.hasText(req.getNickname()) && !req.getNickname().trim().equals(open.getNickname())) {
                open.setNickname(trimTo(req.getNickname(), 64));
                dirty = true;
            }
            if (StringUtils.hasText(req.getEmail()) && !req.getEmail().trim().equals(open.getEmail())) {
                open.setEmail(trimTo(req.getEmail(), 128));
                dirty = true;
            }
            if (memberId != null && open.getMemberId() == null) {
                open.setMemberId(memberId);
                dirty = true;
            }
            if (dirty) {
                chatSessionService.updateById(open);
            }
            return toSessionVO(open, false);
        }

        ChatSession session = new ChatSession();
        session.setSessionNo(genSessionNo());
        session.setAccessToken(UUID.randomUUID().toString().replace("-", ""));
        session.setVisitorId(visitorId);
        session.setMemberId(memberId);
        session.setNickname(trimTo(req.getNickname(), 64));
        session.setEmail(trimTo(req.getEmail(), 128));
        session.setStatus("OPEN");
        session.setUnreadAdmin(0);
        session.setUnreadVisitor(0);
        chatSessionService.save(session);
        return toSessionVO(session, false);
    }

    @Override
    public ChatSessionVO getByAccessToken(String accessToken) {
        ChatSession session = findByToken(accessToken);
        return session == null ? null : toSessionVO(session, false);
    }

    @Override
    public ChatSessionVO requireByAccessToken(String accessToken) {
        ChatSession session = findByToken(accessToken);
        if (session == null) {
            throw new BizException(401, "会话无效，请重新打开咨询");
        }
        return toSessionVO(session, false);
    }

    @Override
    public List<ChatMessageVO> listMessages(Long sessionId, String accessToken) {
        ChatSession session = requireOwned(sessionId, accessToken);
        return loadMessages(session.getId());
    }

    @Override
    @Transactional
    public ChatMessageVO visitorSend(Long sessionId, String accessToken, String content) {
        ChatSession session = requireOwned(sessionId, accessToken);
        if ("CLOSED".equalsIgnoreCase(session.getStatus())) {
            throw new BizException("会话已关闭，请重新发起咨询");
        }
        String text = validateContent(content);
        ChatMessage msg = saveMessage(session.getId(), "VISITOR", null, text);
        bumpSessionAfterMessage(session, text, true);
        return toMessageVO(msg);
    }

    @Override
    @Transactional
    public ChatMessageVO adminReply(Long sessionId, Long adminId, String content) {
        ChatSession session = requireSession(sessionId);
        if ("CLOSED".equalsIgnoreCase(session.getStatus())) {
            throw new BizException("会话已关闭");
        }
        String text = validateContent(content);
        ChatMessage msg = saveMessage(session.getId(), "ADMIN", adminId, text);
        bumpSessionAfterMessage(session, text, false);
        return toMessageVO(msg);
    }

    @Override
    public PageResult<ChatSessionVO> adminPage(String status, Integer page, Integer size) {
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size < 1 ? 20 : Math.min(size, 100);
        LambdaQueryWrapper<ChatSession> qw = new LambdaQueryWrapper<ChatSession>()
                .orderByDesc(ChatSession::getLastMessageAt)
                .orderByDesc(ChatSession::getUpdatedAt);
        if (StringUtils.hasText(status) && !"ALL".equalsIgnoreCase(status)) {
            qw.eq(ChatSession::getStatus, status.trim().toUpperCase());
        }
        Page<ChatSession> result = chatSessionService.page(new Page<>(p + 1L, s), qw);
        List<ChatSessionVO> content = result.getRecords().stream()
                .map(row -> toSessionVO(row, false))
                .toList();
        return PageResult.of(content, result.getTotal(), p, s);
    }

    @Override
    public ChatSessionVO adminGet(Long sessionId) {
        return toSessionVO(requireSession(sessionId), false);
    }

    @Override
    public List<ChatMessageVO> adminListMessages(Long sessionId) {
        requireSession(sessionId);
        return loadMessages(sessionId);
    }

    @Override
    @Transactional
    public ChatSessionVO adminClose(Long sessionId) {
        ChatSession session = requireSession(sessionId);
        session.setStatus("CLOSED");
        chatSessionService.updateById(session);
        return toSessionVO(session, false);
    }

    @Override
    @Transactional
    public ChatSessionVO visitorClose(Long sessionId, String accessToken) {
        ChatSession session = requireOwned(sessionId, accessToken);
        session.setStatus("CLOSED");
        chatSessionService.updateById(session);
        return toSessionVO(session, false);
    }

    @Override
    @Transactional
    public void markAdminRead(Long sessionId) {
        ChatSession session = requireSession(sessionId);
        if (session.getUnreadAdmin() != null && session.getUnreadAdmin() > 0) {
            session.setUnreadAdmin(0);
            chatSessionService.updateById(session);
        }
    }

    @Override
    @Transactional
    public void markVisitorRead(Long sessionId, String accessToken) {
        ChatSession session = requireOwned(sessionId, accessToken);
        if (session.getUnreadVisitor() != null && session.getUnreadVisitor() > 0) {
            session.setUnreadVisitor(0);
            chatSessionService.updateById(session);
        }
    }

    private void bumpSessionAfterMessage(ChatSession session, String text, boolean fromVisitor) {
        LocalDateTime now = LocalDateTime.now();
        session.setLastMessageAt(now);
        session.setLastPreview(text.length() > 120 ? text.substring(0, 120) : text);
        if (fromVisitor) {
            int unread = session.getUnreadAdmin() == null ? 0 : session.getUnreadAdmin();
            session.setUnreadAdmin(unread + 1);
        } else {
            int unread = session.getUnreadVisitor() == null ? 0 : session.getUnreadVisitor();
            session.setUnreadVisitor(unread + 1);
        }
        chatSessionService.updateById(session);
    }

    private ChatMessage saveMessage(Long sessionId, String senderType, Long senderId, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setSenderType(senderType);
        msg.setSenderId(senderId);
        msg.setContent(content);
        chatMessageService.save(msg);
        return msg;
    }

    private List<ChatMessageVO> loadMessages(Long sessionId) {
        return chatMessageService.list(new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getId))
                .stream()
                .map(this::toMessageVO)
                .toList();
    }

    private ChatSession findByToken(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            return null;
        }
        return chatSessionService.getOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getAccessToken, accessToken.trim())
                .last("LIMIT 1"));
    }

    private ChatSession requireOwned(Long sessionId, String accessToken) {
        ChatSession session = requireSession(sessionId);
        if (!StringUtils.hasText(accessToken) || !accessToken.trim().equals(session.getAccessToken())) {
            throw new BizException(401, "会话凭证无效");
        }
        return session;
    }

    private ChatSession requireSession(Long sessionId) {
        ChatSession session = chatSessionService.getById(sessionId);
        if (session == null) {
            throw new BizException("会话不存在");
        }
        return session;
    }

    private static String validateContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw new BizException("请输入消息内容");
        }
        String text = content.trim();
        if (text.isEmpty()) {
            throw new BizException("请输入消息内容");
        }
        if (text.length() > MAX_CONTENT_LEN) {
            throw new BizException("消息不能超过 " + MAX_CONTENT_LEN + " 字");
        }
        return text;
    }

    private ChatSessionVO toSessionVO(ChatSession s, boolean withMessages) {
        ChatSessionVO.ChatSessionVOBuilder b = ChatSessionVO.builder()
                .id(s.getId())
                .sessionNo(s.getSessionNo())
                .accessToken(s.getAccessToken())
                .visitorId(s.getVisitorId())
                .memberId(s.getMemberId())
                .nickname(s.getNickname())
                .email(s.getEmail())
                .status(s.getStatus())
                .lastMessageAt(format(s.getLastMessageAt()))
                .lastPreview(s.getLastPreview())
                .unreadAdmin(s.getUnreadAdmin() == null ? 0 : s.getUnreadAdmin())
                .unreadVisitor(s.getUnreadVisitor() == null ? 0 : s.getUnreadVisitor())
                .createdAt(format(s.getCreatedAt()));
        if (withMessages) {
            b.messages(loadMessages(s.getId()));
        }
        return b.build();
    }

    private ChatMessageVO toMessageVO(ChatMessage m) {
        return ChatMessageVO.builder()
                .id(m.getId())
                .sessionId(m.getSessionId())
                .senderType(m.getSenderType())
                .content(m.getContent())
                .createdAt(format(m.getCreatedAt()))
                .build();
    }

    private static String format(LocalDateTime t) {
        return t == null ? null : FMT.format(t);
    }

    private static String trimTo(String v, int max) {
        if (!StringUtils.hasText(v)) {
            return null;
        }
        String t = v.trim();
        return t.length() > max ? t.substring(0, max) : t;
    }

    private static String genSessionNo() {
        return "CS" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
