package com.tesim.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tesim.common.BizException;
import com.tesim.dto.admin.AdminOrderShippingRequest;
import com.tesim.entity.FulfillmentTask;
import com.tesim.entity.SalesOrder;
import com.tesim.entity.SalesOrderEsim;
import com.tesim.entity.SalesOrderNotice;
import com.tesim.entity.SalesOrderShipment;
import com.tesim.service.FulfillmentTaskService;
import com.tesim.service.OrderAppService;
import com.tesim.service.OrderFulfillmentAppService;
import com.tesim.service.SalesOrderEsimService;
import com.tesim.service.SalesOrderNoticeService;
import com.tesim.service.SalesOrderService;
import com.tesim.service.SalesOrderShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderFulfillmentAppServiceImpl implements OrderFulfillmentAppService {

    private final SalesOrderService salesOrderService;
    private final SalesOrderShipmentService shipmentService;
    private final SalesOrderEsimService esimService;
    private final SalesOrderNoticeService noticeService;
    private final FulfillmentTaskService fulfillmentTaskService;
    private final OrderAppService orderAppService;

    @Override
    @Transactional
    public Map<String, Object> manualFulfill(Long orderId, AdminOrderShippingRequest req) {
        if (req == null) {
            throw new BizException("请填写发货信息");
        }
        SalesOrder order = require(orderId);
        String st = order.getStatus();
        if ("PENDING_PAYMENT".equals(st) || "CANCELLED".equals(st)) {
            throw new BizException("当前订单状态不可履约");
        }
        if ("COMPLETED".equals(st)) {
            throw new BizException("订单已完成");
        }

        String carrier = trim(req.getShippingCarrier());
        String tracking = trim(req.getTrackingNumber());
        String qr = trim(req.getTrackingQrCode());
        String phone = trim(req.getShippingPhone());
        String remark = trim(req.getShippingRemark());

        if (!StringUtils.hasText(tracking) && !StringUtils.hasText(qr)) {
            throw new BizException("请至少填写物流单号或 eSIM 激活码/二维码内容");
        }

        LocalDateTime now = LocalDateTime.now();

        SalesOrderShipment ship = ensureShipment(orderId);
        if (carrier != null) {
            ship.setCarrierCode(carrier);
        }
        if (tracking != null) {
            ship.setTrackingNo(tracking);
        }
        if (phone != null) {
            ship.setShippingPhone(phone);
        }
        if (remark != null) {
            ship.setShippingRemark(remark);
        }
        ship.setShipStatus("SHIPPED");
        ship.setShippedAt(now);
        shipmentService.updateById(ship);

        if (StringUtils.hasText(qr)) {
            SalesOrderEsim esim = esimService.getOne(new LambdaQueryWrapper<SalesOrderEsim>()
                    .eq(SalesOrderEsim::getOrderId, orderId).last("LIMIT 1"));
            if (esim == null) {
                esim = new SalesOrderEsim();
                esim.setOrderId(orderId);
                esim.setEmailSent(false);
            }
            esim.setQrPayload(qr);
            esim.setActivationCode(qr);
            esim.setStatus("DELIVERED");
            esim.setDeliveredAt(now);
            if (esim.getId() == null) {
                esimService.save(esim);
            } else {
                esimService.updateById(esim);
            }
        }

        order.setStatus("SHIPPED");
        salesOrderService.updateById(order);

        FulfillmentTask task = ensureTask(orderId);
        task.setTaskStatus("SUCCESS");
        task.setExternalRef(StringUtils.hasText(tracking) ? tracking : null);
        task.setErrorMessage(null);
        task.setUpdatedAt(now);
        fulfillmentTaskService.updateById(task);

        orderAppService.ensureSubscriptions(order);

        SalesOrderNotice notice = new SalesOrderNotice();
        notice.setOrderId(orderId);
        notice.setNoticeType("MANUAL_FULFILL");
        notice.setSubject("手动发货");
        notice.setContent(buildNotice(carrier, tracking, qr, remark));
        notice.setSentAt(now);
        noticeService.save(notice);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "已手动发货并标记为已发货");
        result.put("orderNo", order.getOrderNo());
        result.put("status", order.getStatus());
        return result;
    }

    private String buildNotice(String carrier, String tracking, String qr, String remark) {
        StringBuilder sb = new StringBuilder("手动履约完成");
        if (StringUtils.hasText(carrier)) {
            sb.append("；承运商=").append(carrier);
        }
        if (StringUtils.hasText(tracking)) {
            sb.append("；单号=").append(tracking);
        }
        if (StringUtils.hasText(qr)) {
            sb.append("；已录入激活码/二维码");
        }
        if (StringUtils.hasText(remark)) {
            sb.append("；备注=").append(remark);
        }
        return sb.toString();
    }

    private SalesOrder require(Long id) {
        SalesOrder order = salesOrderService.getById(id);
        if (order == null || Boolean.TRUE.equals(order.getDeleted())) {
            throw new BizException("订单不存在");
        }
        return order;
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

    private FulfillmentTask ensureTask(Long orderId) {
        FulfillmentTask task = fulfillmentTaskService.getOne(new LambdaQueryWrapper<FulfillmentTask>()
                .eq(FulfillmentTask::getOrderId, orderId).last("LIMIT 1"));
        if (task == null) {
            task = new FulfillmentTask();
            task.setOrderId(orderId);
            task.setTaskStatus("PENDING");
            task.setRetryCount(0);
            fulfillmentTaskService.save(task);
        }
        return task;
    }

    private static String trim(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}
