package com.tesim.controller;

import com.tesim.common.ApiResponse;
import com.tesim.dto.coupon.ValidateCouponRequest;
import com.tesim.dto.coupon.ValidateCouponVO;
import com.tesim.service.CouponAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponAppService couponAppService;

    @PostMapping("/validate")
    public ApiResponse<ValidateCouponVO> validate(@RequestBody ValidateCouponRequest body) {
        return ApiResponse.ok(couponAppService.validateRequest(body));
    }
}
