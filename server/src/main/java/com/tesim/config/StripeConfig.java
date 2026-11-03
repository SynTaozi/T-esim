package com.tesim.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StripeConfig {

    private final StripeProperties stripeProperties;

    @PostConstruct
    public void init() {
        if (stripeProperties.isConfigured()) {
            String key = stripeProperties.resolveApiKey();
            Stripe.apiKey = key;
            String masked = key.length() > 12
                    ? key.substring(0, 8) + "..." + key.substring(key.length() - 4)
                    : "(short)";
            log.info("[Stripe] 已加载 api-key: {}, currency: {}, successUrl: {}",
                    masked, stripeProperties.getCurrency(), stripeProperties.getSuccessUrl());
        } else {
            log.warn("[Stripe] tesim.payment.stripe.api-key 为空，沙箱支付不可用");
        }
    }
}
