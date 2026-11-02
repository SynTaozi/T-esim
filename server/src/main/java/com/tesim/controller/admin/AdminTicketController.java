package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.AdminTicketReplyRequest;
import com.tesim.dto.ticket.TicketVO;
import com.tesim.service.AdminTicketAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/tickets")
@RequiredArgsConstructor
public class AdminTicketController {

    private final AdminTicketAppService ticketAppService;

    @GetMapping
    public ApiResponse<PageResult<TicketVO>> page(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(ticketAppService.page(page, size, status));
    }

    @GetMapping("/status/{status}")
    public ApiResponse<PageResult<TicketVO>> byStatus(
            @PathVariable String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok(ticketAppService.page(page, size, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<TicketVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(ticketAppService.get(id));
    }

    @PostMapping("/{id}/reply")
    public ApiResponse<TicketVO> reply(@PathVariable Long id, @RequestBody AdminTicketReplyRequest req) {
        return ApiResponse.ok(ticketAppService.reply(id, req));
    }

    @PostMapping("/{id}/close")
    public ApiResponse<TicketVO> close(@PathVariable Long id) {
        return ApiResponse.ok(ticketAppService.close(id));
    }
}
