package com.tesim.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Data
@Component
@ConfigurationProperties(prefix = "tesim.payment.stripe")
public class StripeProperties {

    /** Stripe Secret Key sk_test_... */
    private String apiKey = "";
    private String webhookSecret = "";
    private String successUrl = "http://localhost:5173/checkout/stripe/success";
    private String cancelUrl = "http://localhost:5173/checkout/stripe/cancel";
    private String currency = "usd";

    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }
}
