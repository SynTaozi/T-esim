package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.AdminDingtalkConfigRequest;
import com.tesim.dto.admin.AdminDingtalkConfigVO;
import com.tesim.dto.admin.AdminDingtalkLogVO;
import com.tesim.service.AdminDingtalkAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/dingtalk")
@RequiredArgsConstructor
public class AdminDingtalkController {

    private final AdminDingtalkAppService dingtalkAppService;

    @GetMapping("/configs")
    public ApiResponse<List<AdminDingtalkConfigVO>> listConfigs() {
        return ApiResponse.ok(dingtalkAppService.listConfigs());
    }

    @GetMapping("/configs/{id}")
    public ApiResponse<AdminDingtalkConfigVO> getConfig(@PathVariable Long id) {
        return ApiResponse.ok(dingtalkAppService.getConfig(id));
    }

    @PostMapping("/configs")
    public ApiResponse<AdminDingtalkConfigVO> createConfig(@RequestBody AdminDingtalkConfigRequest req) {
        return ApiResponse.ok(dingtalkAppService.createConfig(req));
    }

    @PutMapping("/configs/{id}")
    public ApiResponse<AdminDingtalkConfigVO> updateConfig(@PathVariable Long id,
                                                           @RequestBody AdminDingtalkConfigRequest req) {
        return ApiResponse.ok(dingtalkAppService.updateConfig(id, req));
    }

    @DeleteMapping("/configs/{id}")
    public ApiResponse<Void> deleteConfig(@PathVariable Long id) {
        dingtalkAppService.deleteConfig(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/configs/{id}/test")
    public ApiResponse<Map<String, Object>> test(@PathVariable Long id) {
        return ApiResponse.ok(dingtalkAppService.testSend(id));
    }

    @GetMapping("/logs")
    public ApiResponse<PageResult<AdminDingtalkLogVO>> logs(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok(dingtalkAppService.pageLogs(type, page, size));
    }
}
