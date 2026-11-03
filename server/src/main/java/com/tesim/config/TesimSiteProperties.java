package com.tesim.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 公网站点地址：支付跳转 / Webhook 都必须指向真实可访问域名，不能瞎猜。
 */
@Data
@Component
@ConfigurationProperties(prefix = "tesim.site")
public class TesimSiteProperties {

    /**
     * 前台根地址，如 https://shop.example.com（不要末尾斜杠）
     * 支付成功/取消后浏览器会跳回：{public-web-base}/checkout/stripe/success|cancel
     */
    private String publicWebBase = "http://localhost:5173";

    /**
     * 后端 API 公网根（含 context-path /api），如 https://api.example.com/api
     * Stripe Dashboard Webhook 应填：{public-api-base}/payment/stripe/webhook
     */
    private String publicApiBase = "http://localhost:8088/api";

    public String normalizeBase(String base) {
        if (!StringUtils.hasText(base)) {
            return "";
        }
        String b = base.trim();
        while (b.endsWith("/")) {
            b = b.substring(0, b.length() - 1);
        }
        return b;
    }

    public String webBase() {
        return normalizeBase(publicWebBase);
    }

    public String apiBase() {
        return normalizeBase(publicApiBase);
    }

    public String stripeSuccessUrl() {
        return webBase() + "/checkout/stripe/success";
    }

    public String stripeCancelUrl() {
        return webBase() + "/checkout/stripe/cancel";
    }

    public String stripeWebhookUrl() {
        return apiBase() + "/payment/stripe/webhook";
    }
}
