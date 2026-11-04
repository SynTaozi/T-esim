package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.AdminEmailSendLogVO;
import com.tesim.service.AdminEmailAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/email-send-logs")
@RequiredArgsConstructor
public class AdminEmailSendLogController {

    private final AdminEmailAppService emailAppService;

    @GetMapping
    public ApiResponse<PageResult<AdminEmailSendLogVO>> page(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) String toEmail,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok(emailAppService.pageSendLogs(status, bizType, toEmail, page, size));
    }
}
