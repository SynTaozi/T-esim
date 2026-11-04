package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.AdminOrderAppService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.AdminCustomerNoticeRequest;
import com.tesim.dto.admin.AdminOrderShippingRequest;
import com.tesim.dto.order.OrderVO;
import com.tesim.entity.SalesOrder;
import com.tesim.entity.SalesOrderEsim;
import com.tesim.entity.SalesOrderNotice;
import com.tesim.entity.SalesOrderPayment;
import com.tesim.entity.SalesOrderShipment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminOrderAppServiceImpl implements AdminOrderAppService {

    private final SalesOrderService salesOrderService;
    private final SalesOrderShipmentService shipmentService;
    private final SalesOrderNoticeService noticeService;
    private final SalesOrderPaymentService paymentService;
    private final SalesOrderEsimService esimService;
    private final OrderAppService orderAppService;
    private final OrderFulfillmentAppService orderFulfillmentAppService;
    private final OrderPaymentExpireService orderPaymentExpireService;
    private final BusinessMailFacade businessMailFacade;

    public PageResult<OrderVO> page(String status, Integer page, Integer size, String orderNo,
                                    String email, String thirdPartyOrderNo, Boolean excludeCancelled) {
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size < 1 ? 10 : size;
        LambdaQueryWrapper<SalesOrder> qw = new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getDeleted, false)
                .orderByDesc(SalesOrder::getCreatedAt);
        if (StringUtils.hasText(status)) {
            qw.eq(SalesOrder::getStatus, normalizeStatus(status));
        } else if (excludeCancelled == null || excludeCancelled) {
            qw.ne(SalesOrder::getStatus, "CANCELLED");
        }
        if (StringUtils.hasText(orderNo)) {
            qw.like(SalesOrder::getOrderNo, orderNo.trim());
        }
        if (StringUtils.hasText(email)) {
            qw.like(SalesOrder::getBuyerEmail, email.trim());
        }
        if (StringUtils.hasText(thirdPartyOrderNo)) {
            List<Long> orderIds = paymentService.list(new LambdaQueryWrapper<SalesOrderPayment>()
                            .like(SalesOrderPayment::getThirdPartyTradeNo, thirdPartyOrderNo.trim()))
                    .stream().map(SalesOrderPayment::getOrderId).distinct().toList();
            if (orderIds.isEmpty()) {
                return PageResult.empty(p, s);
            }
            qw.in(SalesOrder::getId, orderIds);
        }
        Page<SalesOrder> result = salesOrderService.page(new Page<>(p + 1L, s), qw);
        List<OrderVO> content = result.getRecords().stream()
                .map(o -> orderAppService.toFrontOrder(o, true))
                .toList();
        return PageResult.of(content, result.getTotal(), p, s);
    }

    public OrderVO detail(Long id) {
        return orderAppService.toFrontOrder(require(id), true);
    }

    @Transactional
    public OrderVO updateStatus(Long id, String status) {
        if (!StringUtils.hasText(status)) {
            throw new BizException("缺少状态");
        }
        SalesOrder order = require(id);
        String next = normalizeStatus(status);
        order.setStatus(next);
        LocalDateTime now = LocalDateTime.now();
        if ("PAID".equals(next) && order.getPaidAt() == null) {
            order.setPaidAt(now);
            if (order.getPaidAmount() == null) {
                order.setPaidAmount(order.getPayableAmount());
            }
        }
        if ("CANCELLED".equals(next)) {
            order.setCancelledAt(now);
            orderPaymentExpireService.clear(id);
        }
        if ("PAID".equals(next) || "COMPLETED".equals(next) || "SHIPPED".equals(next)) {
            orderPaymentExpireService.clear(id);
        }
        if ("COMPLETED".equals(next)) {
            order.setCompletedAt(now);
        }
        if ("SHIPPED".equals(next)) {
            ensureShipment(order.getId()).setShipStatus("SHIPPED");
            SalesOrderShipment ship = ensureShipment(order.getId());
            if (ship.getShippedAt() == null) {
                ship.setShippedAt(now);
            }
            shipmentService.updateById(ship);
        }
        salesOrderService.updateById(order);
        return orderAppService.toFrontOrder(order, true);
    }

    @Transactional
    public OrderVO updateShipping(Long id, AdminOrderShippingRequest req) {
        SalesOrder order = require(id);
        SalesOrderShipment ship = ensureShipment(order.getId());
        if (req.getShippingPhone() != null) {
            ship.setShippingPhone(req.getShippingPhone());
        }
        if (req.getTrackingNumber() != null) {
            ship.setTrackingNo(req.getTrackingNumber());
        }
        if (req.getShippingCarrier() != null) {
            ship.setCarrierCode(req.getShippingCarrier());
        }
        if (req.getShippingRemark() != null) {
            ship.setShippingRemark(req.getShippingRemark());
        }
        boolean newlyShipped = false;
        if (StringUtils.hasText(ship.getTrackingNo()) && !"SHIPPED".equals(ship.getShipStatus())) {
            ship.setShipStatus("SHIPPED");
            ship.setShippedAt(LocalDateTime.now());
            newlyShipped = true;
            if ("PAID".equals(order.getStatus()) || "PROCESSING".equals(order.getStatus())) {
                order.setStatus("SHIPPED");
                salesOrderService.updateById(order);
            }
        }
        shipmentService.updateById(ship);
        // 仅在首次变为已发货时自动发信；重复通知走 resendNotification
        if (newlyShipped) {
            SalesOrderEsim esim = esimService.getOne(new LambdaQueryWrapper<SalesOrderEsim>()
                    .eq(SalesOrderEsim::getOrderId, order.getId()).last("LIMIT 1"));
            businessMailFacade.sendOrderShipping(order, ship, esim);
            ship.setEmailSent(true);
            shipmentService.updateById(ship);
            if (esim != null) {
                esim.setEmailSent(true);
                esimService.updateById(esim);
            }
        }
        return orderAppService.toFrontOrder(order, true);
    }

    @Transactional
    public Map<String, Object> resendNotification(Long id) {
        SalesOrder order = require(id);
        SalesOrderEsim esim = esimService.getOne(new LambdaQueryWrapper<SalesOrderEsim>()
                .eq(SalesOrderEsim::getOrderId, order.getId()).last("LIMIT 1"));
        SalesOrderShipment ship = shipmentService.getOne(new LambdaQueryWrapper<SalesOrderShipment>()
                .eq(SalesOrderShipment::getOrderId, order.getId()).last("LIMIT 1"));
        businessMailFacade.sendOrderShipping(order, ship, esim);
        if (esim != null) {
            esim.setEmailSent(true);
            esimService.updateById(esim);
        }
        if (ship != null) {
            ship.setEmailSent(true);
            shipmentService.updateById(ship);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "发货通知已加入发送队列");
        result.put("orderNo", order.getOrderNo());
        return result;
    }

    @Transactional
    public OrderVO sendCustomerNotice(Long id, AdminCustomerNoticeRequest req) {
        if (req == null || !StringUtils.hasText(req.getContent())) {
            throw new BizException("通知内容不能为空");
        }
        SalesOrder order = require(id);
        String subject = StringUtils.hasText(req.getSubject()) ? req.getSubject() : "订单通知";
        SalesOrderNotice notice = new SalesOrderNotice();
        notice.setOrderId(order.getId());
        notice.setNoticeType("CUSTOMER");
        notice.setSubject(subject);
        notice.setContent(req.getContent().trim());
        notice.setSentAt(LocalDateTime.now());
        noticeService.save(notice);
        businessMailFacade.sendOrderCustomerNotice(order, subject, req.getContent().trim());
        return orderAppService.toFrontOrder(order, true);
    }

    @Transactional
    public Map<String, Object> manualFulfill(Long id, AdminOrderShippingRequest req) {
        return orderFulfillmentAppService.manualFulfill(id, req);
    }

    private SalesOrderShipment ensureShipment(Long orderId) {
        SalesOrderShipment ship = shipmentService.getOne(new LambdaQueryWrapper<SalesOrderShipment>()
                .eq(SalesOrderShipment::getOrderId, orderId).last("LIMIT 1"));
        if (ship == null) {
            ship = new SalesOrderShipment();
            ship.setOrderId(orderId);
            ship.setShipStatus("PENDING");
            ship.setEmailSent(false);
            shipmentService.save(ship);
        }
        return ship;
    }

    private SalesOrder require(Long id) {
        SalesOrder order = salesOrderService.getById(id);
        if (order == null || Boolean.TRUE.equals(order.getDeleted())) {
            throw new BizException("订单不存在");
        }
        return order;
    }

    private static String normalizeStatus(String status) {
        String s = status.trim().toUpperCase();
        return switch (s) {
            case "0", "PENDING", "PENDING_PAYMENT" -> "PENDING_PAYMENT";
            case "1", "PAID" -> "PAID";
            case "PROCESSING", "FULFILLING" -> s;
            case "2", "SHIPPED" -> "SHIPPED";
            case "3", "COMPLETED" -> "COMPLETED";
            case "4", "CANCELLED", "CANCELED" -> "CANCELLED";
            default -> s;
        };
    }
}
