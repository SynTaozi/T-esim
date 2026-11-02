package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.dto.admin.AdminPaymentMethodRequest;
import com.tesim.dto.admin.AdminPaymentMethodVO;
import com.tesim.service.AdminCmsAdminAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/payment-methods")
@RequiredArgsConstructor
public class AdminPaymentChannelController {

    private final AdminCmsAdminAppService cmsAppService;

    @GetMapping
    public ApiResponse<List<AdminPaymentMethodVO>> listAll() {
        return ApiResponse.ok(cmsAppService.listPaymentMethods(false));
    }

    @GetMapping("/enabled")
    public ApiResponse<List<AdminPaymentMethodVO>> listEnabled() {
        return ApiResponse.ok(cmsAppService.listPaymentMethods(true));
    }

    @PostMapping
    public ApiResponse<AdminPaymentMethodVO> save(@RequestBody AdminPaymentMethodRequest req) {
        return ApiResponse.ok(cmsAppService.savePaymentMethod(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminPaymentMethodVO> update(@PathVariable Long id, @RequestBody AdminPaymentMethodRequest req) {
        req.setId(id);
        return ApiResponse.ok(cmsAppService.savePaymentMethod(req));
    }

    @PutMapping("/{id}/enabled")
    public ApiResponse<Void> updateEnabled(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Boolean enabled = body != null && body.get("enabled") != null
                ? Boolean.valueOf(String.valueOf(body.get("enabled")))
                : null;
        cmsAppService.updatePaymentEnabled(id, enabled);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        cmsAppService.deletePaymentMethod(id);
        return ApiResponse.ok(null);
    }
}
