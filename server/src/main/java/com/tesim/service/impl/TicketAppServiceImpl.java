package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.TicketAppService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.ticket.CreateTicketRequest;
import com.tesim.dto.ticket.TicketMessageVO;
import com.tesim.dto.ticket.TicketReplyRequest;
import com.tesim.dto.ticket.TicketVO;
import com.tesim.entity.Member;
import com.tesim.entity.Ticket;
import com.tesim.entity.TicketMessage;
import com.tesim.security.SecurityUtils;
import com.tesim.util.LocaleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TicketAppServiceImpl implements TicketAppService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TicketService ticketService;
    private final TicketMessageService ticketMessageService;
    private final MemberService memberService;
    private final DingtalkNotifyService dingtalkNotifyService;

    @Transactional
    public TicketVO create(CreateTicketRequest req) {
        Long memberId = SecurityUtils.requireMemberId();
        String subject = req.getSubject() == null ? null : req.getSubject().trim();
        String content = req.getContent() == null ? null : req.getContent().trim();
        if (!StringUtils.hasText(subject) || !StringUtils.hasText(content)) {
            throw new BizException("请填写主题和内容");
        }
        Ticket ticket = new Ticket();
        ticket.setTicketNo(genTicketNo());
        ticket.setMemberId(memberId);
        ticket.setOrderId(req.getOrderId());
        ticket.setCategory(StringUtils.hasText(req.getType()) ? req.getType().trim().toUpperCase() : "GENERAL");
        ticket.setSubject(subject);
        ticket.setLocale(LocaleHelper.current());
        ticket.setPriority(StringUtils.hasText(req.getPriority()) ? req.getPriority().trim().toUpperCase() : "MEDIUM");
        ticket.setStatus("OPEN");
        ticketService.save(ticket);

        TicketMessage msg = new TicketMessage();
        msg.setTicketId(ticket.getId());
        msg.setSenderType("USER");
        msg.setSenderId(memberId);
        msg.setSenderName("User");
        msg.setContent(appendImageUrls(content, req.getImageUrls()));
        ticketMessageService.save(msg);

        Member member = memberService.getById(memberId);
        String userEmail = member == null ? null : member.getEmail();
        dingtalkNotifyService.notifyNewTicket(ticket, userEmail);

        return toFront(ticket, true);
    }

    public PageResult<TicketVO> list(Integer page, Integer size, String status) {
        Long memberId = SecurityUtils.requireMemberId();
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size < 1 ? 10 : size;
        LambdaQueryWrapper<Ticket> qw = new LambdaQueryWrapper<Ticket>()
                .eq(Ticket::getMemberId, memberId)
                .orderByDesc(Ticket::getUpdatedAt);
        if (StringUtils.hasText(status) && !"ALL".equalsIgnoreCase(status)) {
            qw.eq(Ticket::getStatus, status.toUpperCase());
        }
        Page<Ticket> result = ticketService.page(new Page<>(p + 1L, s), qw);
        List<TicketVO> content = result.getRecords().stream().map(t -> toFront(t, false)).toList();
        return PageResult.of(content, result.getTotal(), p, s);
    }

    public TicketVO getById(Long id) {
        return toFront(requireOwned(id), true);
    }

    public TicketVO getByNo(String ticketNo) {
        Ticket ticket = ticketService.getOne(new LambdaQueryWrapper<Ticket>().eq(Ticket::getTicketNo, ticketNo));
        if (ticket == null || !Objects.equals(ticket.getMemberId(), SecurityUtils.requireMemberId())) {
            throw new BizException("工单不存在");
        }
        return toFront(ticket, true);
    }

    @Transactional
    public TicketVO followUp(Long id, TicketReplyRequest req) {
        String content = req == null || req.getContent() == null ? "" : req.getContent().trim();
        List<String> imageUrls = req == null ? null : req.getImageUrls();
        boolean hasImages = imageUrls != null && imageUrls.stream().anyMatch(StringUtils::hasText);
        if (!StringUtils.hasText(content) && !hasImages) {
            throw new BizException("请输入内容或上传图片");
        }
        Ticket ticket = requireOwned(id);
        if ("CLOSED".equals(ticket.getStatus())) {
            throw new BizException("工单已关闭");
        }
        Long memberId = SecurityUtils.requireMemberId();
        TicketMessage msg = new TicketMessage();
        msg.setTicketId(ticket.getId());
        msg.setSenderType("USER");
        msg.setSenderId(memberId);
        msg.setSenderName("User");
        msg.setContent(appendImageUrls(content, imageUrls));
        ticketMessageService.save(msg);

        ticket.setStatus("OPEN");
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketService.updateById(ticket);

        return toFront(ticket, true);
    }

    private Ticket requireOwned(Long id) {
        Ticket ticket = ticketService.getById(id);
        if (ticket == null || !Objects.equals(ticket.getMemberId(), SecurityUtils.requireMemberId())) {
            throw new BizException("工单不存在");
        }
        return ticket;
    }

    private TicketVO toFront(Ticket ticket, boolean withMessages) {
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

    private static String genTicketNo() {
        return "TK" + System.currentTimeMillis() + String.format("%03d", new Random().nextInt(1000));
    }

    private static String format(LocalDateTime t) {
        return t == null ? null : FMT.format(t);
    }

    /** 正文后追加图片 URL 区块，前端按标记拆开展示 */
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
