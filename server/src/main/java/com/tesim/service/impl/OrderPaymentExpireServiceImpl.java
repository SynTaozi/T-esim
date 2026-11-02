package com.tesim.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tesim.entity.SalesOrder;
import com.tesim.entity.SalesOrderPayment;
import com.tesim.service.OrderPaymentExpireService;
import com.tesim.service.SalesOrderPaymentService;
import com.tesim.service.SalesOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPaymentExpireServiceImpl implements OrderPaymentExpireService {

    private static final String KEY_PREFIX = "order:pay:expire:";

    private final StringRedisTemplate redis;
    private final SalesOrderService salesOrderService;
    private final SalesOrderPaymentService salesOrderPaymentService;

    @Override
    public String redisKey(Long orderId) {
        return KEY_PREFIX + orderId;
    }

    @Override
    public void schedule(Long orderId) {
        schedule(orderId, TTL);
    }

    @Override
    public void schedule(Long orderId, Duration ttl) {
        if (orderId == null) {
            return;
        }
        Duration use = ttl == null || ttl.isZero() || ttl.isNegative() ? TTL : ttl;
        try {
            redis.opsForValue().set(redisKey(orderId), "1", use);
        } catch (Exception e) {
            log.warn("[OrderExpire] 写入 Redis 延时键失败 orderId={}: {}", orderId, e.getMessage());
        }
    }

    @Override
    public void clear(Long orderId) {
        if (orderId == null) {
            return;
        }
        try {
            redis.delete(redisKey(orderId));
        } catch (Exception e) {
            log.warn("[OrderExpire] 删除 Redis 延时键失败 orderId={}: {}", orderId, e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean cancelIfUnpaidExpired(Long orderId) {
        if (orderId == null) {
            return false;
        }
        SalesOrder order = salesOrderService.getById(orderId);
        if (order == null || Boolean.TRUE.equals(order.getDeleted())) {
            clear(orderId);
            return false;
        }
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            clear(orderId);
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        SalesOrderPayment payment = salesOrderPaymentService.getOne(new LambdaQueryWrapper<SalesOrderPayment>()
                .eq(SalesOrderPayment::getOrderId, orderId)
                .orderByDesc(SalesOrderPayment::getId)
                .last("LIMIT 1"));

        boolean expired;
        if (payment != null && payment.getExpireAt() != null) {
            expired = !payment.getExpireAt().isAfter(now);
        } else {
            LocalDateTime created = order.getCreatedAt() != null ? order.getCreatedAt() : now;
            expired = !created.plus(TTL).isAfter(now);
        }
        if (!expired) {
            // Redis 提前触发或时钟偏差：按剩余时间重调度
            if (payment != null && payment.getExpireAt() != null && payment.getExpireAt().isAfter(now)) {
                schedule(orderId, Duration.between(now, payment.getExpireAt()));
            }
            return false;
        }

        boolean updated = salesOrderService.update(new LambdaUpdateWrapper<SalesOrder>()
                .eq(SalesOrder::getId, orderId)
                .eq(SalesOrder::getStatus, "PENDING_PAYMENT")
                .set(SalesOrder::getStatus, "CANCELLED")
                .set(SalesOrder::getCancelledAt, now));
        if (!updated) {
            clear(orderId);
            return false;
        }

        if (payment != null && "PENDING".equals(payment.getPayStatus())) {
            payment.setPayStatus("EXPIRED");
            salesOrderPaymentService.updateById(payment);
        }
        clear(orderId);
        log.info("[OrderExpire] 已自动取消超时未支付订单 orderId={} orderNo={}", orderId, order.getOrderNo());
        return true;
    }

    @Override
    @Transactional
    public int cancelExpiredBatch(int limit) {
        int max = Math.max(1, Math.min(limit, 500));
        LocalDateTime now = LocalDateTime.now();
        Set<Long> ids = new HashSet<>();

        List<SalesOrderPayment> expiredPays = salesOrderPaymentService.list(new LambdaQueryWrapper<SalesOrderPayment>()
                .eq(SalesOrderPayment::getPayStatus, "PENDING")
                .isNotNull(SalesOrderPayment::getExpireAt)
                .le(SalesOrderPayment::getExpireAt, now)
                .orderByAsc(SalesOrderPayment::getExpireAt)
                .last("LIMIT " + max));
        for (SalesOrderPayment p : expiredPays) {
            if (p.getOrderId() != null) {
                ids.add(p.getOrderId());
            }
        }

        if (ids.size() < max) {
            List<SalesOrder> stale = salesOrderService.list(new LambdaQueryWrapper<SalesOrder>()
                    .eq(SalesOrder::getStatus, "PENDING_PAYMENT")
                    .eq(SalesOrder::getDeleted, false)
                    .le(SalesOrder::getCreatedAt, now.minus(TTL))
                    .orderByAsc(SalesOrder::getCreatedAt)
                    .last("LIMIT " + (max - ids.size())));
            for (SalesOrder o : stale) {
                ids.add(o.getId());
            }
        }

        int n = 0;
        for (Long id : ids) {
            if (cancelIfUnpaidExpired(id)) {
                n++;
            }
        }
        if (n > 0) {
            log.info("[OrderExpire] 扫库兜底取消 {} 笔超时订单", n);
        }
        return n;
    }
}
