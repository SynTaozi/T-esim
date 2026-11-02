package com.tesim.controller;

import com.tesim.common.ApiResponse;
import com.tesim.common.PageResult;
import com.tesim.dto.ticket.CreateTicketRequest;
import com.tesim.dto.ticket.TicketReplyRequest;
import com.tesim.dto.ticket.TicketVO;
import com.tesim.service.TicketAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketAppService ticketAppService;

    @PostMapping
    public ApiResponse<TicketVO> create(@RequestBody CreateTicketRequest req) {
        return ApiResponse.ok(ticketAppService.create(req));
    }

    @PostMapping("/upload")
    public ApiResponse<TicketVO> createUpload(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String content) {
        CreateTicketRequest req = new CreateTicketRequest();
        req.setType(type);
        req.setPriority(priority);
        req.setOrderId(orderId);
        req.setSubject(subject);
        req.setContent(content);
        return ApiResponse.ok(ticketAppService.create(req));
    }

    @GetMapping
    public ApiResponse<PageResult<TicketVO>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(ticketAppService.list(page, size, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<TicketVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(ticketAppService.getById(id));
    }

    @GetMapping("/no/{ticketNo}")
    public ApiResponse<TicketVO> byNo(@PathVariable String ticketNo) {
        return ApiResponse.ok(ticketAppService.getByNo(ticketNo));
    }

    @PostMapping("/{id}/replies")
    public ApiResponse<TicketVO> followUp(@PathVariable Long id, @RequestBody TicketReplyRequest body) {
        return ApiResponse.ok(ticketAppService.followUp(id, body == null ? null : body.getContent()));
    }
}
