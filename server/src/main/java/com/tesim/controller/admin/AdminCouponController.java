package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.AdminCouponRequest;
import com.tesim.dto.admin.AdminCouponUsageVO;
import com.tesim.dto.admin.AdminCouponVO;
import com.tesim.service.AdminCmsAdminAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final AdminCmsAdminAppService cmsAppService;

    @GetMapping
    public ApiResponse<List<AdminCouponVO>> list() {
        return ApiResponse.ok(cmsAppService.listCoupons());
    }

    @PostMapping
    public ApiResponse<AdminCouponVO> create(@RequestBody AdminCouponRequest req) {
        return ApiResponse.ok(cmsAppService.createCoupon(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminCouponVO> update(@PathVariable Long id, @RequestBody AdminCouponRequest req) {
        return ApiResponse.ok(cmsAppService.updateCoupon(id, req));
    }

    @PutMapping("/{id}/disable")
    public ApiResponse<AdminCouponVO> disable(@PathVariable Long id) {
        return ApiResponse.ok(cmsAppService.disableCoupon(id));
    }

    @GetMapping("/usages")
    public ApiResponse<PageResult<AdminCouponUsageVO>> usages(
            @RequestParam(required = false) Long couponId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok(cmsAppService.pageCouponUsages(couponId, page, size));
    }
}
