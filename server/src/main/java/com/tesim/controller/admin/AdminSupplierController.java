package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.dto.admin.AdminSupplierConfigRequest;
import com.tesim.dto.admin.AdminSupplierConfigVO;
import com.tesim.dto.admin.AdminSupplierMappingRequest;
import com.tesim.dto.admin.AdminSupplierMappingVO;
import com.tesim.service.AdminSupplierAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * FE path: /admin/card-platform (Talk2All analogue).
 * Also exposed under /admin/talk2all and /admin/supplier via companion controllers.
 */
@RestController
@RequestMapping({"/admin/card-platform", "/admin/talk2all", "/admin/supplier"})
@RequiredArgsConstructor
public class AdminSupplierController {

    private final AdminSupplierAppService supplierAppService;

    @GetMapping("/configs")
    public ApiResponse<List<AdminSupplierConfigVO>> listConfigs() {
        return ApiResponse.ok(supplierAppService.listConfigs());
    }

    @PostMapping("/configs")
    public ApiResponse<AdminSupplierConfigVO> createConfig(@RequestBody AdminSupplierConfigRequest req) {
        return ApiResponse.ok(supplierAppService.createConfig(req));
    }

    @PutMapping("/configs/{id}")
    public ApiResponse<AdminSupplierConfigVO> updateConfig(@PathVariable Long id,
                                                           @RequestBody AdminSupplierConfigRequest req) {
        return ApiResponse.ok(supplierAppService.updateConfig(id, req));
    }

    @DeleteMapping("/configs/{id}")
    public ApiResponse<Void> deleteConfig(@PathVariable Long id) {
        supplierAppService.deleteConfig(id);
        return ApiResponse.ok("已删除", null);
    }

    @PostMapping("/configs/{id}/test")
    public ApiResponse<Map<String, Object>> testConfig(@PathVariable Long id) {
        return ApiResponse.ok(supplierAppService.testConfig(id));
    }

    @PutMapping("/configs/{id}/environment")
    public ApiResponse<AdminSupplierConfigVO> switchEnv(
            @PathVariable Long id,
            @RequestParam(required = false) String environment,
            @RequestBody(required = false) Map<String, Object> body) {
        String env = environment;
        if ((env == null || env.isBlank()) && body != null && body.get("environment") != null) {
            env = String.valueOf(body.get("environment"));
        }
        return ApiResponse.ok(supplierAppService.switchEnvironment(id, env));
    }

    @GetMapping("/mappings")
    public ApiResponse<List<AdminSupplierMappingVO>> listMappings() {
        return ApiResponse.ok(supplierAppService.listMappings());
    }

    @PostMapping("/mappings")
    public ApiResponse<AdminSupplierMappingVO> createMapping(@RequestBody AdminSupplierMappingRequest req) {
        return ApiResponse.ok(supplierAppService.createMapping(req));
    }

    @PutMapping("/mappings/{id}")
    public ApiResponse<AdminSupplierMappingVO> updateMapping(@PathVariable Long id,
                                                             @RequestBody AdminSupplierMappingRequest req) {
        return ApiResponse.ok(supplierAppService.updateMapping(id, req));
    }

    @DeleteMapping("/mappings/{id}")
    public ApiResponse<Void> deleteMapping(@PathVariable Long id) {
        supplierAppService.deleteMapping(id);
        return ApiResponse.ok("已删除", null);
    }

    @GetMapping("/configs/{configId}/mappings/bulk-preview")
    public ApiResponse<Map<String, Object>> bulkPreview(@PathVariable Long configId) {
        return ApiResponse.ok(supplierAppService.bulkPreview(configId, Map.of()));
    }

    @PostMapping("/mappings/bulk-apply")
    public ApiResponse<Map<String, Object>> bulkApply(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(supplierAppService.bulkApply(body));
    }

    @DeleteMapping("/mappings/batch")
    public ApiResponse<Void> batchDeleteMappings(@RequestBody List<Long> ids) {
        supplierAppService.batchDeleteMappings(ids);
        return ApiResponse.ok("已删除", null);
    }

    @GetMapping("/configs/{configId}/terminal-plans")
    public ApiResponse<Map<String, Object>> terminalPlans(@PathVariable Long configId,
                                                          @RequestParam(required = false) String type) {
        return ApiResponse.ok(supplierAppService.listTerminalPlans(configId, type));
    }

    @GetMapping("/configs/{configId}/terminal-plans/sync-preview")
    public ApiResponse<Map<String, Object>> planSyncPreview(@PathVariable Long configId,
                                                            @RequestParam(required = false) String type) {
        return ApiResponse.ok(supplierAppService.syncPreview(configId, type));
    }

    @PostMapping("/terminal-plans/sync-apply")
    public ApiResponse<Map<String, Object>> planSyncApply(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(supplierAppService.syncApply(body));
    }
}
