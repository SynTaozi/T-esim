package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.dto.admin.AdminSessionVO;
import com.tesim.dto.auth.LoginRequest;
import com.tesim.service.AuthAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AuthAppService authAppService;

    @PostMapping("/login")
    public ApiResponse<AdminSessionVO> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authAppService.adminLogin(req));
    }

    @GetMapping("/me")
    public ApiResponse<AdminSessionVO> me() {
        return ApiResponse.ok(authAppService.adminMe());
    }
}
