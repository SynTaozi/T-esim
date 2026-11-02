package com.tesim.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class LocaleHelper {

    private LocaleHelper() {
    }

    public static String current() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest req = attrs.getRequest();
            String q = req.getParameter("locale");
            if (StringUtils.hasText(q)) {
                return normalize(q);
            }
            String header = req.getHeader("Accept-Language");
            if (StringUtils.hasText(header)) {
                return normalize(header.split(",")[0]);
            }
            String lang = req.getHeader("X-Locale");
            if (StringUtils.hasText(lang)) {
                return normalize(lang);
            }
        }
        return "zh";
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "zh";
        }
        String v = raw.trim().toLowerCase();
        if (v.startsWith("en")) {
            return "en";
        }
        return "zh";
    }

    public static String pick(String zh, String en) {
        return "en".equals(current()) ? (en != null ? en : zh) : (zh != null ? zh : en);
    }
}
