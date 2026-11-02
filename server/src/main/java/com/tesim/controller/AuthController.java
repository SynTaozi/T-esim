package com.tesim.controller;

import com.tesim.common.ApiResponse;
import com.tesim.dto.auth.AuthTokenVO;
import com.tesim.dto.auth.ChangePasswordRequest;
import com.tesim.dto.auth.LoginRequest;
import com.tesim.dto.auth.MemberVO;
import com.tesim.dto.auth.RegisterRequest;
import com.tesim.dto.auth.ResetPasswordRequest;
import com.tesim.dto.auth.SendCodeRequest;
import com.tesim.dto.auth.SendCodeVO;
import com.tesim.dto.auth.UpdateProfileRequest;
import com.tesim.service.AuthAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthAppService authAppService;

    @PostMapping("/login")
    public ApiResponse<AuthTokenVO> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authAppService.login(req));
    }

    @PostMapping("/register")
    public ApiResponse<AuthTokenVO> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok(authAppService.register(req));
    }

    @PostMapping("/send-register-code")
    public ApiResponse<SendCodeVO> sendRegisterCode(@Valid @RequestBody SendCodeRequest req) {
        return ApiResponse.ok(authAppService.sendRegisterCode(req.getEmail()));
    }

    @PostMapping("/send-reset-code")
    public ApiResponse<SendCodeVO> sendResetCode(@Valid @RequestBody SendCodeRequest req) {
        return ApiResponse.ok(authAppService.sendResetCode(req.getEmail()));
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authAppService.resetPassword(req);
        return ApiResponse.ok("密码已重置", null);
    }

    @GetMapping("/profile")
    public ApiResponse<MemberVO> profile() {
        return ApiResponse.ok(authAppService.profile());
    }

    @PutMapping("/profile")
    public ApiResponse<MemberVO> updateProfile(@RequestBody UpdateProfileRequest req) {
        return ApiResponse.ok(authAppService.updateProfile(req));
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        authAppService.changePassword(req);
        return ApiResponse.ok("密码已修改", null);
    }
}
