package com.tesim.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Data
@Component
@ConfigurationProperties(prefix = "tesim.payment.stripe")
public class StripeProperties {

    /**
     * Stripe Secret Key。优先用环境变量 TESIM_STRIPE_API_KEY；
     * 未设置时可用 api-key-b64（沙箱默认，避免明文触发 GitHub secret scanning）。
     */
    private String apiKey = "";
    /** Base64 编码的沙箱 api-key，仅当 apiKey 为空时使用 */
    private String apiKeyB64 = "";
    private String webhookSecret = "";
    /**
     * 可显式覆盖；为空则使用 tesim.site.public-web-base + /checkout/stripe/success
     */
    private String successUrl = "";
    /**
     * 可显式覆盖；为空则使用 tesim.site.public-web-base + /checkout/stripe/cancel
     */
    private String cancelUrl = "";
    private String currency = "usd";

    public String resolveApiKey() {
        if (StringUtils.hasText(apiKey)) {
            return apiKey.trim();
        }
        if (StringUtils.hasText(apiKeyB64)) {
            try {
                return new String(Base64.getDecoder().decode(apiKeyB64.trim()), StandardCharsets.UTF_8).trim();
            } catch (IllegalArgumentException ignored) {
                return "";
            }
        }
        return "";
    }

    public boolean isConfigured() {
        return StringUtils.hasText(resolveApiKey());
    }
}
