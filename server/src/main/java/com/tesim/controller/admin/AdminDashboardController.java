package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.dto.admin.DashboardOverviewVO;
import com.tesim.dto.admin.DashboardStatsVO;
import com.tesim.service.AdminDashboardAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardAppService dashboardAppService;

    @GetMapping("/stats")
    public ApiResponse<DashboardStatsVO> stats() {
        return ApiResponse.ok(dashboardAppService.stats());
    }

    @GetMapping("/overview")
    public ApiResponse<DashboardOverviewVO> overview() {
        return ApiResponse.ok(dashboardAppService.overview());
    }
}
