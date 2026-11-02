package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.dto.admin.AdminAccountUpdateRequest;
import com.tesim.dto.admin.AdminSessionVO;
import com.tesim.service.AuthAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/account")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AuthAppService authAppService;

    @GetMapping
    public ApiResponse<AdminSessionVO> get() {
        return ApiResponse.ok(authAppService.adminMe());
    }

    @PutMapping
    public ApiResponse<AdminSessionVO> update(@Valid @RequestBody AdminAccountUpdateRequest req) {
        return ApiResponse.ok("账号已更新", authAppService.updateAdminAccount(req));
    }
}
