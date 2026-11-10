package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
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
}
