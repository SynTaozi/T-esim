package com.tesim.service;

import com.tesim.entity.Member;
import com.tesim.entity.SalesOrder;
import com.tesim.entity.SalesOrderEsim;
import com.tesim.entity.SalesOrderShipment;
import com.tesim.entity.Ticket;
import com.tesim.mail.MailBizTypes;
import com.tesim.mail.MailMessage;
import com.tesim.mail.MailPurposes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 业务侧邮件封装：订单发货 / 客服通知 / 工单回复等。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessMailFacade {

    private final MailSendService mailSendService;

    public void sendOrderShipping(SalesOrder order, SalesOrderShipment ship, SalesOrderEsim esim) {
        if (order == null || !StringUtils.hasText(order.getBuyerEmail())) {
            log.debug("跳过发货邮件：订单无买家邮箱 orderId={}", order == null ? null : order.getId());
            return;
        }
        String carrier = ship == null ? "" : nullToEmpty(ship.getCarrierCode());
        String tracking = ship == null ? "" : nullToEmpty(ship.getTrackingNo());
        String activation = "";
        if (esim != null) {
            activation = firstNonBlank(esim.getActivationCode(), esim.getQrPayload());
        }
        Map<String, String> vars = new HashMap<>();
        vars.put("orderNo", nullToEmpty(order.getOrderNo()));
        vars.put("carrier", carrier.isEmpty() ? "-" : carrier);
        vars.put("trackingNo", tracking.isEmpty() ? "-" : tracking);
        vars.put("activationCode", activation.isEmpty() ? "-" : activation);

        try {
            mailSendService.sendAsync(MailMessage.builder()
                    .to(order.getBuyerEmail())
                    .purpose(MailPurposes.SHIPPING)
                    .bizType(MailBizTypes.ORDER_SHIPPING)
                    .bizId(order.getOrderNo())
                    .templateCode(MailBizTypes.ORDER_SHIPPING)
                    .locale("zh")
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
        Map<String, String> vars = new HashMap<>();
        vars.put("orderNo", nullToEmpty(order.getOrderNo()));
        vars.put("subject", StringUtils.hasText(subject) ? subject.trim() : "订单通知");
        vars.put("content", content == null ? "" : content.trim().replace("\n", "<br/>"));
        try {
            mailSendService.sendAsync(MailMessage.builder()
                    .to(order.getBuyerEmail())
                    .purpose(MailPurposes.SUPPORT)
                    .bizType(MailBizTypes.ORDER_CUSTOMER_NOTICE)
                    .bizId(order.getOrderNo())
                    .templateCode(MailBizTypes.ORDER_CUSTOMER_NOTICE)
                    .locale("zh")
                    .variables(vars)
                    .subject(vars.get("subject"))
                    .htmlBody("<p>订单 " + vars.get("orderNo") + "</p><div>" + vars.get("content") + "</div>")
                    .build());
        } catch (Exception e) {
            log.warn("排队订单通知邮件失败 orderNo={}: {}", order.getOrderNo(), e.getMessage());
        }
    }

    public void sendTicketReply(Ticket ticket, Member member, String reply) {
        if (ticket == null || member == null || !StringUtils.hasText(member.getEmail())) {
            return;
        }
        Map<String, String> vars = new HashMap<>();
        vars.put("ticketNo", nullToEmpty(ticket.getTicketNo()));
        vars.put("ticketSubject", nullToEmpty(ticket.getSubject()));
        vars.put("reply", reply == null ? "" : reply.trim().replace("\n", "<br/>"));
        String locale = StringUtils.hasText(ticket.getLocale()) ? ticket.getLocale() : "zh";
        try {
            mailSendService.sendAsync(MailMessage.builder()
                    .to(member.getEmail())
                    .purpose(MailPurposes.SUPPORT)
                    .bizType(MailBizTypes.TICKET_REPLY)
                    .bizId(ticket.getTicketNo())
                    .templateCode(MailBizTypes.TICKET_REPLY)
                    .locale(locale)
                    .variables(vars)
                    .subject("【Tesim】工单 " + vars.get("ticketNo") + " 有新回复")
                    .htmlBody("<p>工单 " + vars.get("ticketNo") + "</p><blockquote>" + vars.get("reply") + "</blockquote>")
                    .build());
        } catch (Exception e) {
            log.warn("排队工单回复邮件失败 ticketNo={}: {}", ticket.getTicketNo(), e.getMessage());
        }
    }

    private static String fallbackShippingHtml(Map<String, String> vars) {
        return "<p>您的订单 <strong>" + vars.get("orderNo") + "</strong> 已发货。</p>"
                + "<p>物流公司：" + vars.get("carrier") + "</p>"
                + "<p>物流单号：" + vars.get("trackingNo") + "</p>"
                + "<p>激活信息：" + vars.get("activationCode") + "</p>";
    }

    private static String nullToEmpty(String v) {
        return v == null ? "" : v.trim();
    }

    private static String firstNonBlank(String a, String b) {
        if (StringUtils.hasText(a)) {
            return a.trim();
        }
        if (StringUtils.hasText(b)) {
            return b.trim();
        }
        return "";
    }
}
