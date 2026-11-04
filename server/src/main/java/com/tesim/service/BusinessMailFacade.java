package com.tesim.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tesim.config.TesimSiteProperties;
import com.tesim.entity.Member;
import com.tesim.entity.SalesOrder;
import com.tesim.entity.SalesOrderEsim;
import com.tesim.entity.SalesOrderItem;
import com.tesim.entity.SalesOrderShipment;
import com.tesim.entity.Ticket;
import com.tesim.mail.MailBizTypes;
import com.tesim.mail.MailMessage;
import com.tesim.mail.MailPurposes;
import com.tesim.mail.MailTemplateRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 业务侧邮件封装：变量名对齐后台 HTML 模板（{{var}} / ${VAR} / {{#if}}）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessMailFacade {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final MailSendService mailSendService;
    private final SalesOrderItemService salesOrderItemService;
    private final TesimSiteProperties siteProperties;

    public void sendOrderShipping(SalesOrder order, SalesOrderShipment ship, SalesOrderEsim esim) {
        if (order == null || !StringUtils.hasText(order.getBuyerEmail())) {
            log.debug("跳过发货邮件：订单无买家邮箱 orderId={}", order == null ? null : order.getId());
            return;
        }
        String carrier = ship == null ? "" : nullToEmpty(ship.getCarrierCode());
        String tracking = ship == null ? "" : nullToEmpty(ship.getTrackingNo());
        String phone = ship == null ? "" : nullToEmpty(ship.getShippingPhone());
        String remark = ship == null ? "" : nullToEmpty(ship.getShippingRemark());
        String activation = "";
        if (esim != null) {
            activation = firstNonBlank(esim.getQrPayload(), esim.getActivationCode());
        }
        String qrCodeUrl = looksLikeUrl(activation) ? activation : "";
        // 非 URL 的激活码塞进备注，避免 {{#if qrCodeUrl}} 空白又丢信息
        if (!StringUtils.hasText(qrCodeUrl) && StringUtils.hasText(activation)) {
            String tip = "激活信息: " + activation;
            remark = StringUtils.hasText(remark) ? remark + "；" + tip : tip;
        }
        LocalDateTime shippedAt = ship != null && ship.getShippedAt() != null
                ? ship.getShippedAt()
                : LocalDateTime.now();

        Map<String, String> vars = new HashMap<>();
        put(vars, "orderNo", nullToEmpty(order.getOrderNo()));
        put(vars, "packageName", resolvePackageName(order));
        put(vars, "shippingCarrier", carrier);
        put(vars, "trackingNumber", tracking);
        put(vars, "phone", phone);
        put(vars, "qrCodeUrl", qrCodeUrl);
        put(vars, "remark", remark);
        put(vars, "shippingTime", DT.format(shippedAt));
        // 兼容旧模板
        put(vars, "carrier", carrier);
        put(vars, "trackingNo", tracking);
        put(vars, "activationCode", activation);

        try {
            mailSendService.sendAsync(MailMessage.builder()
                    .to(order.getBuyerEmail())
                    .purpose(MailPurposes.SHIPPING)
                    .bizType(MailBizTypes.ORDER_SHIPPING)
                    .bizId(order.getOrderNo())
                    .templateCode(MailBizTypes.ORDER_SHIPPING)
                    .locale(localeOf(order.getLocale()))
                    .variables(vars)
                    .subject("【Tesim】订单 " + order.getOrderNo() + " 已发货")
                    .htmlBody(fallbackShippingHtml(vars))
                    .build());
        } catch (Exception e) {
            log.warn("排队发货邮件失败 orderNo={}: {}", order.getOrderNo(), e.getMessage());
        }
    }

    public void sendOrderCustomerNotice(SalesOrder order, String subject, String content) {
        if (order == null || !StringUtils.hasText(order.getBuyerEmail())) {
            return;
        }
        String htmlContent = content == null ? "" : content.trim().replace("\n", "<br/>");
        Map<String, String> vars = new HashMap<>();
        put(vars, "orderNo", nullToEmpty(order.getOrderNo()));
        put(vars, "subject", StringUtils.hasText(subject) ? subject.trim() : "订单通知");
        put(vars, "noticeContent", htmlContent);
        put(vars, "content", htmlContent);
        try {
            mailSendService.sendAsync(MailMessage.builder()
                    .to(order.getBuyerEmail())
                    .purpose(MailPurposes.SUPPORT)
                    .bizType(MailBizTypes.ORDER_CUSTOMER_NOTICE)
                    .bizId(order.getOrderNo())
                    .templateCode(MailBizTypes.ORDER_CUSTOMER_NOTICE)
                    .locale(localeOf(order.getLocale()))
                    .variables(vars)
                    .subject(vars.get("subject"))
                    .htmlBody("<p>订单 " + vars.get("orderNo") + "</p><div>" + htmlContent + "</div>")
                    .build());
        } catch (Exception e) {
            log.warn("排队订单通知邮件失败 orderNo={}: {}", order.getOrderNo(), e.getMessage());
        }
    }

    public void sendTicketReply(Ticket ticket, Member member, String reply, String repliedBy) {
        if (ticket == null || member == null || !StringUtils.hasText(member.getEmail())) {
            return;
        }
        String htmlReply = reply == null ? "" : reply.trim().replace("\n", "<br/>");
        String ticketUrl = siteProperties.webBase() + "/ticket/" + ticket.getId();
        Map<String, String> vars = new HashMap<>();
        put(vars, "ticketNo", nullToEmpty(ticket.getTicketNo()));
        put(vars, "subject", nullToEmpty(ticket.getSubject()));
        put(vars, "ticketSubject", nullToEmpty(ticket.getSubject()));
        put(vars, "replyContent", htmlReply);
        put(vars, "reply", htmlReply);
        put(vars, "repliedBy", StringUtils.hasText(repliedBy) ? repliedBy.trim() : "Support");
        put(vars, "ticketUrl", ticketUrl);

        try {
            mailSendService.sendAsync(MailMessage.builder()
                    .to(member.getEmail())
                    .purpose(MailPurposes.SUPPORT)
                    .bizType(MailBizTypes.TICKET_REPLY)
                    .bizId(ticket.getTicketNo())
                    .templateCode(MailBizTypes.TICKET_REPLY)
                    .locale(localeOf(ticket.getLocale()))
                    .variables(vars)
                    .subject("【Tesim】工单 " + vars.get("ticketNo") + " 有新回复")
                    .htmlBody("<p>工单 " + vars.get("ticketNo") + "</p><blockquote>" + htmlReply + "</blockquote>")
                    .build());
        } catch (Exception e) {
            log.warn("排队工单回复邮件失败 ticketNo={}: {}", ticket.getTicketNo(), e.getMessage());
        }
    }

    /** 兼容旧调用 */
    public void sendTicketReply(Ticket ticket, Member member, String reply) {
        sendTicketReply(ticket, member, reply, null);
    }

    private String resolvePackageName(SalesOrder order) {
        if (order == null || order.getId() == null) {
            return "-";
        }
        List<SalesOrderItem> items = salesOrderItemService.list(new LambdaQueryWrapper<SalesOrderItem>()
                .eq(SalesOrderItem::getOrderId, order.getId())
                .orderByAsc(SalesOrderItem::getId));
        if (items.isEmpty()) {
            return "-";
        }
        boolean en = "en".equalsIgnoreCase(localeOf(order.getLocale()));
        for (SalesOrderItem item : items) {
            String kind = item.getProductKind() == null ? "" : item.getProductKind().toUpperCase(Locale.ROOT);
            if (kind.contains("SHIP") || kind.contains("DEVICE") || kind.contains("EXPRESS")) {
                continue;
            }
            String title = en
                    ? firstNonBlank(item.getTitleEn(), item.getTitleZh(), item.getSkuCode())
                    : firstNonBlank(item.getTitleZh(), item.getTitleEn(), item.getSkuCode());
            if (StringUtils.hasText(title)) {
                return title;
            }
        }
        SalesOrderItem first = items.get(0);
        return en
                ? firstNonBlank(first.getTitleEn(), first.getTitleZh(), first.getSkuCode(), "-")
                : firstNonBlank(first.getTitleZh(), first.getTitleEn(), first.getSkuCode(), "-");
    }

    private static void put(Map<String, String> vars, String key, String value) {
        MailTemplateRenderer.put(vars, key, value);
    }

    private static String fallbackShippingHtml(Map<String, String> vars) {
        return "<p>您的订单 <strong>" + vars.getOrDefault("orderNo", "") + "</strong> 已发货。</p>"
                + "<p>套餐：" + vars.getOrDefault("packageName", "-") + "</p>"
                + "<p>物流公司：" + vars.getOrDefault("shippingCarrier", "-") + "</p>"
                + "<p>物流单号：" + vars.getOrDefault("trackingNumber", "-") + "</p>"
                + "<p>发货时间：" + vars.getOrDefault("shippingTime", "-") + "</p>";
    }

    private static String localeOf(String locale) {
        if (!StringUtils.hasText(locale)) {
            return "zh";
        }
        return locale.toLowerCase(Locale.ROOT).startsWith("en") ? "en" : "zh";
    }

    private static boolean looksLikeUrl(String v) {
        if (!StringUtils.hasText(v)) {
            return false;
        }
        String t = v.trim().toLowerCase(Locale.ROOT);
        return t.startsWith("http://") || t.startsWith("https://");
    }

    private static String nullToEmpty(String v) {
        return v == null ? "" : v.trim();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v.trim();
            }
        }
        return "";
    }
}
