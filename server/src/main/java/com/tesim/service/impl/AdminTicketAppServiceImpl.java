package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.AdminTicketAppService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.AdminTicketReplyRequest;
import com.tesim.dto.ticket.TicketMessageVO;
import com.tesim.dto.ticket.TicketVO;
import com.tesim.entity.Member;
import com.tesim.entity.Ticket;
import com.tesim.entity.TicketMessage;
import com.tesim.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTicketAppServiceImpl implements AdminTicketAppService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TicketService ticketService;
    private final TicketMessageService ticketMessageService;
    private final MemberService memberService;
    private final BusinessMailFacade businessMailFacade;

    public PageResult<TicketVO> page(Integer page, Integer size, String status) {
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size < 1 ? 10 : size;
        LambdaQueryWrapper<Ticket> qw = new LambdaQueryWrapper<Ticket>()
                .orderByDesc(Ticket::getUpdatedAt);
        if (StringUtils.hasText(status) && !"ALL".equalsIgnoreCase(status)) {
            qw.eq(Ticket::getStatus, status.toUpperCase());
        }
        Page<Ticket> result = ticketService.page(new Page<>(p + 1L, s), qw);
        List<TicketVO> content = result.getRecords().stream().map(t -> toVO(t, false)).toList();
        return PageResult.of(content, result.getTotal(), p, s);
    }

    public TicketVO get(Long id) {
        return toVO(require(id), true);
    }

    @Transactional
    public TicketVO reply(Long id, AdminTicketReplyRequest req) {
        String replyText = req == null ? null : req.resolveReply();
        boolean hasImages = req != null && req.getImageUrls() != null
                && req.getImageUrls().stream().anyMatch(StringUtils::hasText);
        if (!StringUtils.hasText(replyText) && !hasImages) {
            throw new BizException("请输入回复内容或上传图片");
        }
        Ticket ticket = require(id);
        if ("CLOSED".equalsIgnoreCase(ticket.getStatus())) {
            throw new BizException("工单已关闭");
        }
        Long adminId = SecurityUtils.memberIdOrNull();
        Member admin = adminId != null ? memberService.getById(adminId) : null;
        TicketMessage msg = new TicketMessage();
        msg.setTicketId(ticket.getId());
        msg.setSenderType("ADMIN");
        msg.setSenderId(adminId);
        msg.setSenderName(StringUtils.hasText(req.getRepliedBy())
                ? req.getRepliedBy()
                : (admin != null && StringUtils.hasText(admin.getUsername()) ? admin.getUsername() : "Admin"));
        msg.setContent(appendImageUrls(replyText == null ? "" : replyText, req.getImageUrls()));
        ticketMessageService.save(msg);

        ticket.setStatus("OPEN");
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketService.updateById(ticket);

        if (ticket.getMemberId() != null) {
            Member owner = memberService.getById(ticket.getMemberId());
            businessMailFacade.sendTicketReply(ticket, owner,
                    StringUtils.hasText(replyText) ? replyText : "[图片]", msg.getSenderName());
        }
        return toVO(ticket, true);
    }

    @Transactional
    public TicketVO close(Long id) {
        Ticket ticket = require(id);
        ticket.setStatus("CLOSED");
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketService.updateById(ticket);
        return toVO(ticket, true);
    }

    private Ticket require(Long id) {
        Ticket t = ticketService.getById(id);
        if (t == null) {
            throw new BizException("工单不存在");
        }
        return t;
    }

    private TicketVO toVO(Ticket ticket, boolean withMessages) {
        List<TicketMessage> messages = ticketMessageService.list(new LambdaQueryWrapper<TicketMessage>()
                .eq(TicketMessage::getTicketId, ticket.getId())
                .orderByAsc(TicketMessage::getCreatedAt));
        String content = messages.isEmpty() ? "" : messages.get(0).getContent();
        List<TicketMessageVO> replies = null;
        if (withMessages) {
            replies = new ArrayList<>();
            for (int i = 1; i < messages.size(); i++) {
                TicketMessage msg = messages.get(i);
                replies.add(TicketMessageVO.builder()
                        .id(msg.getId())
                        .content(msg.getContent())
                        .admin("ADMIN".equalsIgnoreCase(msg.getSenderType()))
                        .senderType(msg.getSenderType())
                        .createdAt(format(msg.getCreatedAt()))
                        .build());
            }
        }
        return TicketVO.builder()
                .id(ticket.getId())
                .ticketNo(ticket.getTicketNo())
                .userId(ticket.getMemberId())
                .orderId(ticket.getOrderId())
                .type(ticket.getCategory())
                .category(ticket.getCategory())
                .subject(ticket.getSubject())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .content(content)
                .createdAt(format(ticket.getCreatedAt()))
                .updatedAt(format(ticket.getUpdatedAt()))
                .replies(replies)
                .build();
    }

    private static String format(LocalDateTime t) {
        return t == null ? null : FMT.format(t);
    }

    private static String appendImageUrls(String content, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return content;
        }
        StringBuilder sb = new StringBuilder(content == null ? "" : content);
        sb.append("\n\n__IMAGES__\n");
        for (String url : imageUrls) {
            if (!StringUtils.hasText(url)) {
                continue;
            }
            String u = url.trim();
            if (u.startsWith("/api/uploads/") || u.startsWith("/uploads/")) {
                sb.append(u).append('\n');
            }
        }
        return sb.toString().trim();
    }
}
