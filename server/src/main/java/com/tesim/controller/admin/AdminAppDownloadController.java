package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.dto.admin.AdminAppDownloadRequest;
import com.tesim.dto.admin.AdminAppDownloadVO;
import com.tesim.service.AdminCmsAdminAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/app-download-qrcodes")
@RequiredArgsConstructor
public class AdminAppDownloadController {

    private final AdminCmsAdminAppService cmsAppService;

    @GetMapping
    public ApiResponse<List<AdminAppDownloadVO>> list() {
        return ApiResponse.ok(cmsAppService.listAppDownloads());
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminAppDownloadVO> get(@PathVariable Long id) {
        return ApiResponse.ok(cmsAppService.getAppDownload(id));
    }

    @PostMapping
    public ApiResponse<AdminAppDownloadVO> create(@RequestBody AdminAppDownloadRequest req) {
        return ApiResponse.ok(cmsAppService.createAppDownload(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminAppDownloadVO> update(@PathVariable Long id, @RequestBody AdminAppDownloadRequest req) {
        return ApiResponse.ok(cmsAppService.updateAppDownload(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        cmsAppService.deleteAppDownload(id);
        return ApiResponse.ok(null);
    }
}
