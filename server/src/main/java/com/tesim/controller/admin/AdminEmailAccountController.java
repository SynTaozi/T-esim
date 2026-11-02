package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.AdminEmailAccountRequest;
import com.tesim.dto.admin.AdminEmailAccountVO;
import com.tesim.service.AdminEmailAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/email-accounts")
@RequiredArgsConstructor
public class AdminEmailAccountController {

    private final AdminEmailAppService emailAppService;

    @GetMapping
    public ApiResponse<List<AdminEmailAccountVO>> list() {
        return ApiResponse.ok(emailAppService.listAccounts());
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminEmailAccountVO> get(@PathVariable Long id) {
        return ApiResponse.ok(emailAppService.getAccount(id));
    }

    @PostMapping
    public ApiResponse<AdminEmailAccountVO> create(@RequestBody AdminEmailAccountRequest req) {
        return ApiResponse.ok(emailAppService.createAccount(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminEmailAccountVO> update(@PathVariable Long id, @RequestBody AdminEmailAccountRequest req) {
        return ApiResponse.ok(emailAppService.updateAccount(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        emailAppService.deleteAccount(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/test")
    public ApiResponse<Map<String, Object>> test(@PathVariable Long id) {
        return ApiResponse.ok(emailAppService.testAccount(id));
    }

    @GetMapping("/{accountId}/emails")
    public ApiResponse<PageResult<Map<String, Object>>> emails(
            @PathVariable Long accountId,
            @RequestParam(required = false, defaultValue = "INBOX") String folderType,
            @RequestParam(required = false) Integer start,
            @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(emailAppService.listEmails(accountId, folderType, start, limit));
    }

    @GetMapping("/{accountId}/emails/{messageId}")
    public ApiResponse<Map<String, Object>> getEmail(
            @PathVariable Long accountId,
            @PathVariable String messageId,
            @RequestParam(required = false, defaultValue = "INBOX") String folderType) {
        return ApiResponse.ok(emailAppService.getEmail(accountId, messageId, folderType));
    }

    @PostMapping("/{accountId}/emails/send")
    public ApiResponse<Map<String, Object>> send(
            @PathVariable Long accountId,
            @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(emailAppService.sendEmail(accountId, body));
    }

    @PutMapping("/{accountId}/emails/{messageId}/read")
    public ApiResponse<Map<String, Object>> markRead(
            @PathVariable Long accountId,
            @PathVariable String messageId) {
        return ApiResponse.ok(emailAppService.markRead(accountId, messageId));
    }

    @DeleteMapping("/{accountId}/emails/{messageId}")
    public ApiResponse<Map<String, Object>> deleteEmail(
            @PathVariable Long accountId,
            @PathVariable String messageId) {
        return ApiResponse.ok(emailAppService.deleteEmail(accountId, messageId));
    }
}
