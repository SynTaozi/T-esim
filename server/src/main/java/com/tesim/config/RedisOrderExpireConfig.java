package com.tesim.config;

import com.tesim.service.OrderPaymentExpireService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisOrderExpireConfig {

    private static final String KEY_PREFIX = "order:pay:expire:";

    private final StringRedisTemplate redis;
    private final OrderPaymentExpireService orderPaymentExpireService;

    @PostConstruct
    public void enableKeyspaceEvents() {
        try {
            var factory = redis.getConnectionFactory();
            if (factory == null) {
                log.warn("[OrderExpire] RedisConnectionFactory 为空，依赖扫库兜底");
                return;
            }
            try (var connection = factory.getConnection()) {
                connection.serverCommands().setConfig("notify-keyspace-events", "Ex");
            }
            log.info("[OrderExpire] 已尝试开启 Redis keyspace 过期通知 (Ex)");
        } catch (Exception e) {
            log.warn("[OrderExpire] 无法设置 notify-keyspace-events，依赖扫库兜底: {}", e.getMessage());
        }
    }

    @Bean
    RedisMessageListenerContainer orderExpireListenerContainer(RedisConnectionFactory factory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(this::onExpired, new PatternTopic("__keyevent@*__:expired"));
        return container;
    }

    private void onExpired(Message message, byte[] pattern) {
        byte[] body = message.getBody();
        String key = body == null ? null : new String(body, StandardCharsets.UTF_8);
        if (!StringUtils.hasText(key) || !key.startsWith(KEY_PREFIX)) {
            return;
        }
        String idPart = key.substring(KEY_PREFIX.length());
        try {
            Long orderId = Long.valueOf(idPart);
            orderPaymentExpireService.cancelIfUnpaidExpired(orderId);
        } catch (Exception e) {
            log.warn("[OrderExpire] 处理过期键失败 key={}: {}", key, e.getMessage());
        }
    }
}
