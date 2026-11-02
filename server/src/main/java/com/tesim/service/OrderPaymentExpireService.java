package com.tesim.service;

import java.time.Duration;

/**
 * 待支付订单 30 分钟自动取消：Redis 延时为主，定时扫库兜底。
 */
public interface OrderPaymentExpireService {

    Duration TTL = Duration.ofMinutes(30);

    String redisKey(Long orderId);

    /** 下单/续期支付窗口时写入或刷新 Redis 过期键 */
    void schedule(Long orderId);

    /** 按指定 TTL 刷新（秒），用于支付窗口被延长时 */
    void schedule(Long orderId, Duration ttl);

    /** 支付成功或主动取消时删除延时键 */
    void clear(Long orderId);

    /**
     * 尝试取消过期未支付订单（幂等）。
     * @return true 表示本次完成了取消
     */
    boolean cancelIfUnpaidExpired(Long orderId);

    /** 扫库兜底：取消已过 expire_at / 创建超过 30 分钟仍待支付的订单 */
    int cancelExpiredBatch(int limit);
}
