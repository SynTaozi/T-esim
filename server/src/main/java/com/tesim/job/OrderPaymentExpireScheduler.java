package com.tesim.job;

import com.tesim.service.OrderPaymentExpireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Redis 过期通知的兜底：定期扫描并取消超时未支付订单。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentExpireScheduler {

    private final OrderPaymentExpireService orderPaymentExpireService;

    @Scheduled(fixedDelayString = "${tesim.order.expire-scan-ms:300000}")
    public void scanExpiredOrders() {
        try {
            orderPaymentExpireService.cancelExpiredBatch(200);
        } catch (Exception e) {
            log.error("[OrderExpire] 扫库兜底异常: {}", e.getMessage(), e);
        }
    }
}
