package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.dto.admin.AdminThemeRequest;
import com.tesim.service.AdminCmsAdminAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/system/theme")
@RequiredArgsConstructor
public class AdminThemeController {

    private final AdminCmsAdminAppService cmsAppService;

    @GetMapping
    public ApiResponse<Map<String, String>> get() {
        return ApiResponse.ok(cmsAppService.getTheme());
    }

    @PostMapping
    public ApiResponse<Map<String, String>> set(@RequestBody AdminThemeRequest req) {
        return ApiResponse.ok(cmsAppService.setTheme(req));
    }
}
