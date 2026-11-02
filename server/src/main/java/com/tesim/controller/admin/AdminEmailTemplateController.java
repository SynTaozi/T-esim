package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.dto.admin.AdminEmailTemplateRequest;
import com.tesim.dto.admin.AdminEmailTemplateVO;
import com.tesim.service.AdminEmailAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/email-templates")
@RequiredArgsConstructor
public class AdminEmailTemplateController {

    private final AdminEmailAppService emailAppService;

    @GetMapping
    public ApiResponse<List<AdminEmailTemplateVO>> list() {
        return ApiResponse.ok(emailAppService.listTemplates());
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminEmailTemplateVO> get(@PathVariable Long id) {
        return ApiResponse.ok(emailAppService.getTemplate(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminEmailTemplateVO> update(@PathVariable Long id, @RequestBody AdminEmailTemplateRequest req) {
        return ApiResponse.ok(emailAppService.updateTemplate(id, req));
    }

    @PutMapping("/{id}/enabled")
    public ApiResponse<AdminEmailTemplateVO> enabled(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Boolean enabled = body != null && body.get("enabled") != null
                ? Boolean.valueOf(String.valueOf(body.get("enabled")))
                : null;
        return ApiResponse.ok(emailAppService.updateTemplateEnabled(id, enabled));
    }

    @GetMapping("/{id}/preview")
    public ApiResponse<Map<String, Object>> preview(@PathVariable Long id) {
        return ApiResponse.ok(emailAppService.previewTemplate(id));
    }
}
