package com.tesim.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.tesim.common.BizException;
import com.tesim.config.StripeConfig;
import com.tesim.config.StripeProperties;
import com.tesim.dto.payment.CheckoutRedirectVO;
import com.tesim.dto.payment.StripeConfirmVO;
import com.tesim.entity.SalesOrder;
import com.tesim.entity.SalesOrderItem;
import com.tesim.entity.SalesOrderPayment;
import com.tesim.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

/**
 * Stripe Checkout 沙箱：微信 / 支付宝 / 银行卡。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StripePaymentService {

    private final StripeProperties stripeProperties;
    private final StripeConfig stripeConfig;
    private final SalesOrderService salesOrderService;
    private final SalesOrderPaymentService salesOrderPaymentService;
    private final SalesOrderItemService salesOrderItemService;
    private final OrderAppService orderAppService;
    private final OrderPaymentExpireService orderPaymentExpireService;

    public static boolean isStripePaymentMethod(String paymentMethod) {
        if (paymentMethod == null) {
            return false;
        }
        String m = paymentMethod.trim().toUpperCase(Locale.ROOT);
        return "WECHAT".equals(m) || "ALIPAY".equals(m) || "BANK_CARD".equals(m) || "CARD".equals(m);
    }

    @Transactional
    public CheckoutRedirectVO createOrReuseCheckoutSession(String orderNo, Long orderId, String paymentMethod, String email) {
        ensureConfigured();
        String method = normalizeMethod(paymentMethod);
        SalesOrder order = requirePayableOrder(orderNo, orderId);
        SalesOrderPayment payment = ensurePaymentRow(order, method);

        // Reuse open session when possible
        if (StringUtils.hasText(payment.getThirdPartyTradeNo()) && payment.getThirdPartyTradeNo().startsWith("cs_")) {
            try {
                Session existing = Session.retrieve(payment.getThirdPartyTradeNo());
                if ("open".equalsIgnoreCase(existing.getStatus()) && StringUtils.hasText(existing.getUrl())) {
                    log.info("[Stripe] 复用未完成 Session - orderNo={}, sessionId={}", order.getOrderNo(), existing.getId());
                    return CheckoutRedirectVO.builder()
                            .sessionId(existing.getId())
                            .url(existing.getUrl())
                            .checkoutUrl(existing.getUrl())
                            .orderNo(order.getOrderNo())
                            .stub(false)
                            .build();
                }
            } catch (StripeException e) {
                log.warn("[Stripe] 复用 Session 失败，将重新创建 - sessionId={}, err={}",
                        payment.getThirdPartyTradeNo(), e.getMessage());
            }
        }

        String productName = resolveProductName(order.getId());
        String customerEmail = StringUtils.hasText(email) ? email.trim() : order.getBuyerEmail();
        try {
            Session session = createSession(order.getOrderNo(), order.getPayableAmount(), method, productName, customerEmail);
            payment.setChannelCode(method);
            payment.setThirdPartyTradeNo(session.getId());
            payment.setPayStatus("PENDING");
            if (payment.getExpireAt() == null || payment.getExpireAt().isBefore(LocalDateTime.now())) {
                payment.setExpireAt(LocalDateTime.now().plusMinutes(30));
            }
            salesOrderPaymentService.updateById(payment);
            rescheduleExpire(order.getId(), payment.getExpireAt());
            log.info("[Stripe] Checkout Session 已创建 - orderNo={}, method={}, sessionId={}",
                    order.getOrderNo(), method, session.getId());
            return CheckoutRedirectVO.builder()
                    .sessionId(session.getId())
                    .url(session.getUrl())
                    .checkoutUrl(session.getUrl())
                    .orderNo(order.getOrderNo())
                    .stub(false)
                    .build();
        } catch (StripeException e) {
            log.error("[Stripe] 创建 Session 失败 - orderNo={}, code={}, err={}",
                    order.getOrderNo(), e.getCode(), e.getMessage(), e);
            throw new BizException("Stripe 创建支付失败: " + e.getMessage());
        }
    }

    public Event parseAndVerifyWebhook(String payload, String sigHeader) throws SignatureVerificationException {
        ensureConfigured();
        if (!StringUtils.hasText(stripeProperties.getWebhookSecret())) {
            throw new BizException("未配置 Stripe webhook-secret");
        }
        return Webhook.constructEvent(payload, sigHeader, stripeProperties.getWebhookSecret().trim());
    }

    @Transactional
    public StripeConfirmVO confirmBySessionId(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            throw new BizException("session_id 不能为空");
        }
        ensureConfigured();
        try {
            Session session = Session.retrieve(sessionId.trim());
            handleCheckoutSessionCompleted(session);

            String orderNo = metadataOrderNo(session);
            boolean paid = "paid".equalsIgnoreCase(session.getPaymentStatus())
                    || "complete".equalsIgnoreCase(session.getStatus());
            if (StringUtils.hasText(orderNo)) {
                SalesOrder order = orderAppService.findByOrderNo(orderNo);
                if (order != null && !"PENDING_PAYMENT".equals(order.getStatus())
                        && !"CANCELLED".equals(order.getStatus())) {
                    paid = true;
                }
            }
            return StripeConfirmVO.builder()
                    .success(true)
                    .paid(paid)
                    .orderNo(orderNo)
                    .sessionId(session.getId())
                    .paymentStatus(session.getPaymentStatus())
                    .sessionStatus(session.getStatus())
                    .status(paid ? "PAID" : "PENDING")
                    .build();
        } catch (StripeException e) {
            log.error("[Stripe] confirm 失败 - sessionId={}, err={}", sessionId, e.getMessage(), e);
            throw new BizException("确认支付失败: " + e.getMessage());
        }
    }

    public void handleCheckoutSessionCompleted(Session session) {
        if (session == null) {
            return;
        }
        if (StringUtils.hasText(session.getId())
                && (session.getMetadata() == null || session.getMetadata().isEmpty()
                || !StringUtils.hasText(session.getPaymentStatus()))) {
            try {
                session = Session.retrieve(session.getId());
            } catch (StripeException e) {
                log.warn("[Stripe] 重新拉取 Session 失败 - id={}, err={}", session.getId(), e.getMessage());
            }
        }

        String orderNo = metadataOrderNo(session);
        if (!StringUtils.hasText(orderNo)) {
            log.warn("[Stripe] Session 缺少 orderNo - sessionId={}", session.getId());
            return;
        }

        String paymentStatus = session.getPaymentStatus();
        if (paymentStatus != null
                && !"paid".equalsIgnoreCase(paymentStatus)
                && !"no_payment_required".equalsIgnoreCase(paymentStatus)) {
            log.info("[Stripe] Session 尚未 paid，跳过 - orderNo={}, paymentStatus={}", orderNo, paymentStatus);
            return;
        }

        SalesOrder order = orderAppService.findByOrderNo(orderNo);
        if (order == null) {
            log.warn("[Stripe] 订单不存在 - orderNo={}", orderNo);
            return;
        }
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            log.info("[Stripe] 订单已非待支付，跳过 - orderNo={}, status={}", orderNo, order.getStatus());
            return;
        }

        SalesOrderPayment payment = orderAppService.latestPayment(order.getId());
        if (payment != null && StringUtils.hasText(session.getId())) {
            payment.setThirdPartyTradeNo(session.getId());
        }
        String transactionId = session.getPaymentIntent() != null ? session.getPaymentIntent() : session.getId();
        log.info("[Stripe] 确认支付 - orderNo={}, sessionId={}, tx={}", orderNo, session.getId(), transactionId);
        orderAppService.markPaid(order, payment, transactionId);
    }

    private Session createSession(String businessNo,
                                  BigDecimal amountUsd,
                                  String method,
                                  String productName,
                                  String customerEmail) throws StripeException {
        long amountCents = toStripeAmountCents(amountUsd);
        if (amountCents < 50) {
            throw new BizException("支付金额过低，无法发起 Stripe 支付");
        }
        String successBase = stripeConfig.resolveSuccessUrl();
        String cancelBase = stripeConfig.resolveCancelUrl();
        if (!StringUtils.hasText(successBase) || !successBase.startsWith("http")
                || !StringUtils.hasText(cancelBase) || !cancelBase.startsWith("http")) {
            throw new BizException("未配置支付回跳地址，请设置环境变量 TESIM_PUBLIC_WEB_BASE（前台域名，如 https://你的域名）");
        }

        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(appendQuery(successBase, "orderNo", businessNo)
                        + "&session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(appendQuery(cancelBase, "orderNo", businessNo))
                .setClientReferenceId(businessNo)
                .putMetadata("orderNo", businessNo)
                .putMetadata("paymentMethod", method)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(stripeProperties.getCurrency())
                                .setUnitAmount(amountCents)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(StringUtils.hasText(productName) ? productName : "Tesim Order")
                                        .build())
                                .build())
                        .build());

        if (StringUtils.hasText(customerEmail)) {
            builder.setCustomerEmail(customerEmail.trim());
        }
        applyPaymentMethodTypes(builder, method);

        return Session.create(builder.build(),
                com.stripe.net.RequestOptions.builder()
                        .setIdempotencyKey("checkout-" + businessNo + "-" + method + "-" + System.currentTimeMillis())
                        .build());
    }

    private void applyPaymentMethodTypes(SessionCreateParams.Builder builder, String method) {
        switch (method) {
            case "WECHAT" -> {
                builder.addPaymentMethodType(SessionCreateParams.PaymentMethodType.WECHAT_PAY);
                builder.setPaymentMethodOptions(SessionCreateParams.PaymentMethodOptions.builder()
                        .setWechatPay(SessionCreateParams.PaymentMethodOptions.WechatPay.builder()
                                .setClient(SessionCreateParams.PaymentMethodOptions.WechatPay.Client.WEB)
                                .build())
                        .build());
            }
            case "ALIPAY" -> builder.addPaymentMethodType(SessionCreateParams.PaymentMethodType.ALIPAY);
            case "BANK_CARD", "CARD" -> builder.addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD);
            default -> throw new BizException("不支持的 Stripe 支付方式: " + method);
        }
    }

    private SalesOrder requirePayableOrder(String orderNo, Long orderId) {
        Long memberId = SecurityUtils.memberIdOrNull();
        SalesOrder order = null;
        if (StringUtils.hasText(orderNo)) {
            order = orderAppService.findByOrderNo(orderNo.trim());
        }
        if (order == null && orderId != null) {
            order = salesOrderService.getById(orderId);
        }
        if (order == null && StringUtils.hasText(orderNo)) {
            try {
                order = salesOrderService.getById(Long.valueOf(orderNo.trim()));
            } catch (Exception ignored) {
                // not an id
            }
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

    private SalesOrderPayment ensurePaymentRow(SalesOrder order, String method) {
        SalesOrderPayment payment = orderAppService.latestPayment(order.getId());
        if (payment == null) {
            payment = new SalesOrderPayment();
            payment.setOrderId(order.getId());
            payment.setChannelCode(method);
            payment.setPayStatus("PENDING");
            payment.setAmount(order.getPayableAmount());
            payment.setCurrency(order.getCurrency() != null ? order.getCurrency() : "USD");
            payment.setExpireAt(LocalDateTime.now().plusMinutes(30));
            salesOrderPaymentService.save(payment);
            rescheduleExpire(order.getId(), payment.getExpireAt());
        }
        return payment;
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

    private String resolveProductName(Long orderId) {
        SalesOrderItem item = salesOrderItemService.getOne(new LambdaQueryWrapper<SalesOrderItem>()
                .eq(SalesOrderItem::getOrderId, orderId)
                .orderByAsc(SalesOrderItem::getId)
                .last("LIMIT 1"));
        if (item == null) {
            return "Tesim Order";
        }
        if (StringUtils.hasText(item.getTitleEn())) {
            return item.getTitleEn();
        }
        if (StringUtils.hasText(item.getTitleZh())) {
            return item.getTitleZh();
        }
        return "Tesim Order";
    }

    private void ensureConfigured() {
        if (!stripeProperties.isConfigured()) {
            throw new BizException("Stripe 未配置，请设置 tesim.payment.stripe.api-key");
        }
        if (!StringUtils.hasText(com.stripe.Stripe.apiKey)) {
            com.stripe.Stripe.apiKey = stripeProperties.resolveApiKey();
        }
    }

    private static String metadataOrderNo(Session session) {
        String orderNo = null;
        if (session.getMetadata() != null) {
            orderNo = session.getMetadata().get("orderNo");
        }
        if (!StringUtils.hasText(orderNo)) {
            orderNo = session.getClientReferenceId();
        }
        return orderNo;
    }

    private static String normalizeMethod(String paymentMethod) {
        if (!StringUtils.hasText(paymentMethod)) {
            throw new BizException("缺少支付方式");
        }
        String m = paymentMethod.trim().toUpperCase(Locale.ROOT);
        if ("CARD".equals(m)) {
            return "BANK_CARD";
        }
        if (!isStripePaymentMethod(m)) {
            throw new BizException("不支持的 Stripe 支付方式: " + paymentMethod);
        }
        return m;
    }

    private static long toStripeAmountCents(BigDecimal amountUsd) {
        if (amountUsd == null) {
            return 0L;
        }
        return amountUsd.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    private static String appendQuery(String baseUrl, String key, String value) {
        if (!StringUtils.hasText(baseUrl)) {
            return "";
        }
        String sep = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + sep + key + "=" + value;
    }
}
