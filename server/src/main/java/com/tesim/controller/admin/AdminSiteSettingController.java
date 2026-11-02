package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.dto.admin.AdminSiteDisplayRequest;
import com.tesim.dto.cms.SiteDisplayVO;
import com.tesim.service.AdminConfigAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/site-display")
@RequiredArgsConstructor
public class AdminSiteSettingController {

    private final AdminConfigAppService configAppService;

    @GetMapping
    public ApiResponse<SiteDisplayVO> get() {
        return ApiResponse.ok(configAppService.siteDisplay());
    }

    @PutMapping
    public ApiResponse<SiteDisplayVO> update(@RequestBody AdminSiteDisplayRequest req) {
        return ApiResponse.ok(configAppService.updateSiteDisplay(req));
    }
}
