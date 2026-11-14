package com.tesim.controller;

import com.tesim.common.ApiResponse;
import com.tesim.common.BizException;
import com.tesim.dto.payment.*;
import com.tesim.service.PaymentAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentAppService paymentAppService;

    @PostMapping("/usdt/create")
    public ApiResponse<PaymentOrderVO> createUsdt(@RequestBody CreateUsdtPaymentRequest body) {
        return ApiResponse.ok(paymentAppService.createUsdt(body));
    }

    @PostMapping("/usdt/callback")
    public ApiResponse<Void> callback(@RequestBody UsdtCallbackRequest body) {
        paymentAppService.usdtCallback(body);
        return ApiResponse.ok("支付成功", null);
    }

    @GetMapping("/usdt/query/{paymentId}")
    public ApiResponse<PaymentOrderVO> query(@PathVariable String paymentId) {
        return ApiResponse.ok(paymentAppService.queryByPaymentId(paymentId));
    }

    @GetMapping("/usdt/query-by-order/{orderNo}")
    public ApiResponse<PaymentOrderVO> queryByOrder(@PathVariable String orderNo) {
        return ApiResponse.ok(paymentAppService.queryByOrderNo(orderNo));
    }

    @GetMapping("/usdt/address")
    public ApiResponse<UsdtAddressVO> address() {
        return ApiResponse.ok(paymentAppService.usdtAddress());
    }

    @PostMapping("/usdt/simulate-pay")
    public ApiResponse<Void> simulate(@RequestBody SimulatePayRequest body) {
        paymentAppService.simulatePay(body);
        return ApiResponse.ok("支付成功", null);
    }

    @GetMapping("/usdt/debug/pending-tasks")
    public ApiResponse<List<String>> pendingTasks() {
        return ApiResponse.ok(Collections.emptyList());
    }

    @PostMapping("/stripe/checkout-session")
    public ApiResponse<CheckoutRedirectVO> stripe(@RequestBody StripeCheckoutRequest body) {
        return ApiResponse.ok(paymentAppService.stripeCheckoutSession(body));
    }

    @GetMapping("/stripe/confirm")
    public ApiResponse<StripeConfirmVO> confirmStripe(
            @RequestParam(name = "session_id", required = false) String sessionId,
            @RequestParam(name = "sessionId", required = false) String sessionIdAlt) {
        String id = sessionId != null && !sessionId.isBlank() ? sessionId : sessionIdAlt;
        return ApiResponse.ok(paymentAppService.confirmStripe(id));
    }

    /**
     * Stripe Webhook：本地可用
     * stripe listen --forward-to localhost:8080/api/payment/stripe/webhook
     */
    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> stripeWebhook(@RequestBody String payload,
                                                @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        if (sigHeader == null || sigHeader.isBlank()) {
            log.error("[Stripe] Webhook 缺少 Stripe-Signature，拒绝处理");
            return ResponseEntity.badRequest().body("missing signature");
        }
        try {
            paymentAppService.handleStripeWebhook(payload, sigHeader);
            return ResponseEntity.ok("ok");
        } catch (BizException e) {
            log.error("[Stripe] Webhook 业务失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("[Stripe] Webhook 处理失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("error");
        }
    }
}
