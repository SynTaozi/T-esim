package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.AdminMemberVO;
import com.tesim.service.AdminMemberAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminMemberAppService memberAppService;

    @GetMapping
    public ApiResponse<PageResult<AdminMemberVO>> page(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(memberAppService.page(page, size, keyword, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminMemberVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(memberAppService.get(id));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<AdminMemberVO> updateStatus(
            @PathVariable Long id,
            @RequestParam(required = false) String status,
            @RequestBody(required = false) Map<String, Object> body) {
        String s = status;
        if ((s == null || s.isBlank()) && body != null && body.get("status") != null) {
            s = String.valueOf(body.get("status"));
        }
        return ApiResponse.ok(memberAppService.updateStatus(id, s));
    }
}
