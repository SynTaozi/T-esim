package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.AdminCustomerNoticeRequest;
import com.tesim.dto.admin.AdminOrderShippingRequest;
import com.tesim.dto.order.OrderVO;
import com.tesim.service.AdminOrderAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderAppService orderAppService;

    @GetMapping
    public ApiResponse<PageResult<OrderVO>> page(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String thirdPartyOrderNo,
            @RequestParam(required = false) Boolean excludeCancelled) {
        return ApiResponse.ok(orderAppService.page(status, page, size, orderNo, email, thirdPartyOrderNo, excludeCancelled));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(orderAppService.detail(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<OrderVO> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ApiResponse.ok(orderAppService.updateStatus(id, status));
    }

    @PutMapping("/{id}/shipping")
    public ApiResponse<OrderVO> updateShipping(@PathVariable Long id, @RequestBody AdminOrderShippingRequest req) {
        return ApiResponse.ok(orderAppService.updateShipping(id, req));
    }

    @PostMapping("/{id}/resend-notification")
    public ApiResponse<Map<String, Object>> resendNotification(@PathVariable Long id) {
        return ApiResponse.ok(orderAppService.resendNotification(id));
    }

    @PostMapping("/{id}/customer-notice")
    public ApiResponse<OrderVO> customerNotice(@PathVariable Long id, @RequestBody AdminCustomerNoticeRequest req) {
        return ApiResponse.ok(orderAppService.sendCustomerNotice(id, req));
    }

    /** 手动履约发货 */
    @PostMapping("/{id}/fulfill")
    public ApiResponse<Map<String, Object>> fulfill(@PathVariable Long id, @RequestBody AdminOrderShippingRequest req) {
        return ApiResponse.ok(orderAppService.manualFulfill(id, req));
    }
}
