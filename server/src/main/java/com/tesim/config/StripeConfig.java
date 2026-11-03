package com.tesim.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StripeConfig {

    private final StripeProperties stripeProperties;
    private final TesimSiteProperties siteProperties;

    @PostConstruct
    public void init() {
        if (stripeProperties.isConfigured()) {
            String key = stripeProperties.resolveApiKey();
            Stripe.apiKey = key;
            String masked = key.length() > 12
                    ? key.substring(0, 8) + "..." + key.substring(key.length() - 4)
                    : "(short)";
            String success = resolveSuccessUrl();
            String cancel = resolveCancelUrl();
            log.info("[Stripe] 已加载 api-key: {}, currency: {}", masked, stripeProperties.getCurrency());
            log.info("[Stripe] 支付成功跳转(success): {}", success);
            log.info("[Stripe] 支付取消跳转(cancel): {}", cancel);
            log.info("[Stripe] Webhook 回调应配置为: {} （在 Stripe Dashboard 填此地址）",
                    siteProperties.stripeWebhookUrl());
            if (!StringUtils.hasText(siteProperties.webBase())) {
                log.error("[Stripe] tesim.site.public-web-base 未配置！支付成功后无法跳回前台，请设置 TESIM_PUBLIC_WEB_BASE");
            }
            if (!StringUtils.hasText(siteProperties.apiBase())) {
                log.error("[Stripe] tesim.site.public-api-base 未配置！请设置 TESIM_PUBLIC_API_BASE");
            }
        } else {
            log.warn("[Stripe] tesim.payment.stripe.api-key 为空，沙箱支付不可用");
        }
    }

    public String resolveSuccessUrl() {
        if (StringUtils.hasText(stripeProperties.getSuccessUrl())) {
            return stripeProperties.getSuccessUrl().trim();
        }
        return siteProperties.stripeSuccessUrl();
    }

    public String resolveCancelUrl() {
        if (StringUtils.hasText(stripeProperties.getCancelUrl())) {
            return stripeProperties.getCancelUrl().trim();
        }
        return siteProperties.stripeCancelUrl();
    }
}
