package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.PaymentAppService;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.tesim.common.BizException;
import com.tesim.dto.payment.*;
import com.tesim.entity.SalesOrder;
import com.tesim.entity.SalesOrderPayment;
import com.tesim.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentAppServiceImpl implements PaymentAppService {

    private final OrderAppService orderAppService;
    private final SalesOrderPaymentService salesOrderPaymentService;
    private final SalesOrderService salesOrderService;
    private final StripePaymentService stripePaymentService;
    private final OrderPaymentExpireService orderPaymentExpireService;

    @Value("${tesim.payment.usdt.address:TXxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx}")
    private String usdtAddress;

    @Value("${tesim.payment.usdt.rate:1}")
    private BigDecimal usdtRate;

    @Transactional
    public PaymentOrderVO createUsdt(CreateUsdtPaymentRequest req) {
        SalesOrder order = requireOrder(req.getOrderNo(), req.getOrderId());
        SalesOrderPayment payment = orderAppService.latestPayment(order.getId());
        if (payment == null) {
            payment = new SalesOrderPayment();
            payment.setOrderId(order.getId());
            payment.setChannelCode("USDT");
            payment.setPayStatus("PENDING");
            payment.setAmount(order.getPayableAmount());
            payment.setCurrency("USD");
            payment.setExpireAt(LocalDateTime.now().plusMinutes(30));
            salesOrderPaymentService.save(payment);
        } else {
            payment.setChannelCode("USDT");
            payment.setPayStatus("PENDING");
            if (payment.getExpireAt() == null || payment.getExpireAt().isBefore(LocalDateTime.now())) {
                payment.setExpireAt(LocalDateTime.now().plusMinutes(30));
            }
        }
        BigDecimal amount = req.getAmount() != null ? req.getAmount() : order.getPayableAmount();
        BigDecimal crypto = amount.divide(usdtRate == null || usdtRate.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ONE : usdtRate, 6, RoundingMode.HALF_UP);
        payment.setAmount(amount);
        payment.setCryptoAmount(crypto);
        payment.setCryptoAddress(usdtAddress);
        salesOrderPaymentService.updateById(payment);
        rescheduleExpire(order.getId(), payment.getExpireAt());

        return paymentView(order, payment);
    }

    @Transactional
    public void usdtCallback(UsdtCallbackRequest req) {
        SalesOrderPayment payment = findPayment(req.getPaymentId());
        SalesOrder order = salesOrderService.getById(payment.getOrderId());
        if (order == null) {
            throw new BizException("订单不存在");
        }
        orderAppService.markPaid(order, payment, req.getTransactionId());
    }

    @Transactional
    public void simulatePay(SimulatePayRequest req) {
        UsdtCallbackRequest callback = new UsdtCallbackRequest();
        callback.setPaymentId(req.getPaymentId());
        usdtCallback(callback);
    }

    public PaymentOrderVO queryByPaymentId(String paymentId) {
        SalesOrderPayment payment = findPayment(paymentId);
        SalesOrder order = salesOrderService.getById(payment.getOrderId());
        return paymentView(order, payment);
    }

    public PaymentOrderVO queryByOrderNo(String orderNo) {
        SalesOrder order = orderAppService.findByOrderNo(orderNo);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        SalesOrderPayment payment = orderAppService.latestPayment(order.getId());
        return paymentView(order, payment);
    }

    public UsdtAddressVO usdtAddress() {
        return UsdtAddressVO.builder().address(usdtAddress).build();
    }

    public CheckoutRedirectVO stripeCheckoutSession(StripeCheckoutRequest req) {
        return stripePaymentService.createOrReuseCheckoutSession(
                req.getOrderNo(), req.getOrderId(), req.getPaymentMethod(), req.getEmail());
    }

    public StripeConfirmVO confirmStripe(String sessionId) {
        return stripePaymentService.confirmBySessionId(sessionId);
    }

    public void handleStripeWebhook(String payload, String signatureHeader) {
        try {
            var event = stripePaymentService.parseAndVerifyWebhook(payload, signatureHeader);
            log.info("[Stripe] Webhook 事件: {}", event.getType());
            if ("checkout.session.completed".equals(event.getType())
                    || "checkout.session.async_payment_succeeded".equals(event.getType())) {
                Session session = resolveSessionFromEvent(event);
                if (session != null) {
                    stripePaymentService.handleCheckoutSessionCompleted(session);
                } else {
                    log.warn("[Stripe] 无法从事件解析 Session - eventId={}", event.getId());
                }
            } else if ("checkout.session.async_payment_failed".equals(event.getType())) {
                log.warn("[Stripe] 异步支付失败: {}", event.getId());
            }
        } catch (SignatureVerificationException e) {
            throw new BizException("Stripe webhook 签名校验失败");
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("Stripe webhook 处理失败: " + e.getMessage());
        }
    }

    private static Session resolveSessionFromEvent(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        StripeObject obj = deserializer.getObject().orElse(null);
        if (obj instanceof Session session) {
            return session;
        }
        try {
            StripeObject raw = deserializer.deserializeUnsafe();
            if (raw instanceof Session session) {
                return session;
            }
        } catch (Exception ignored) {
            // fall through
        }
        try {
            String json = deserializer.getRawJson();
            if (json != null && json.contains("\"id\"")) {
                int idx = json.indexOf("\"id\"");
                int colon = json.indexOf(':', idx);
                int q1 = json.indexOf('"', colon + 1);
                int q2 = json.indexOf('"', q1 + 1);
                if (q1 > 0 && q2 > q1) {
                    String id = json.substring(q1 + 1, q2);
                    if (id.startsWith("cs_")) {
                        return Session.retrieve(id);
                    }
                }
            }
        } catch (Exception ignored) {
            // ignore
        }
        return null;
    }

    private SalesOrder requireOrder(String orderNo, Long orderId) {
        Long memberId = SecurityUtils.memberIdOrNull();
        SalesOrder order = null;
        if (StringUtils.hasText(orderNo)) {
            order = orderAppService.findByOrderNo(orderNo);
        }
        if (order == null && orderId != null) {
            order = salesOrderService.getById(orderId);
        }
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (memberId != null) {
            if (order.getMemberId() != null && !Objects.equals(order.getMemberId(), memberId)) {
                throw new BizException("订单不存在");
            }
        } else if (order.getMemberId() != null) {
            throw new BizException("请先登录后再支付该订单");
        }
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BizException("订单状态不可支付");
        }
        return order;
    }

    private SalesOrderPayment findPayment(String paymentId) {
        if (!StringUtils.hasText(paymentId)) {
            throw new BizException("缺少 paymentId");
        }
        Long id;
        if (paymentId.startsWith("PAY")) {
            id = toLong(paymentId.substring(3));
        } else {
            id = toLong(paymentId);
        }
        SalesOrderPayment payment = id == null ? null : salesOrderPaymentService.getById(id);
        if (payment == null) {
            throw new BizException("支付单不存在");
        }
        return payment;
    }

    private PaymentOrderVO paymentView(SalesOrder order, SalesOrderPayment payment) {
        Long expireTime = payment != null && payment.getExpireAt() != null
                ? payment.getExpireAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null;
        Long paidTime = payment != null && payment.getPaidAt() != null
                ? payment.getPaidAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null;
        return PaymentOrderVO.builder()
                .paymentId(payment == null ? null : "PAY" + payment.getId())
                .orderNo(order.getOrderNo())
                .orderId(order.getId())
                .amount(payment != null ? payment.getAmount() : order.getPayableAmount())
                .usdtAmount(payment != null ? payment.getCryptoAmount() : null)
                .usdtAddress(payment != null ? payment.getCryptoAddress() : usdtAddress)
                .status(payment != null ? payment.getPayStatus() : "PENDING")
                .expireTime(expireTime)
                .transactionId(payment != null ? payment.getTransactionId() : null)
                .paidTime(paidTime)
                .build();
    }

    private void rescheduleExpire(Long orderId, LocalDateTime expireAt) {
        if (orderId == null) {
            return;
        }
        if (expireAt == null) {
            orderPaymentExpireService.schedule(orderId);
            return;
        }
        Duration left = Duration.between(LocalDateTime.now(), expireAt);
        if (left.isZero() || left.isNegative()) {
            orderPaymentExpireService.schedule(orderId);
        } else {
            orderPaymentExpireService.schedule(orderId, left);
        }
    }

    private static String normalize(String method) {
        if (!StringUtils.hasText(method)) {
            return "USDT";
        }
        return method.trim().toUpperCase();
    }

    private static Long toLong(Object v) {
        if (v == null || !StringUtils.hasText(String.valueOf(v))) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(v).replace(".0", ""));
        } catch (Exception e) {
            return null;
        }
    }
}
