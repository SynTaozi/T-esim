package com.tesim.mail;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板渲染：支持 {{var}}、${VAR}，以及简单的 {{#if var}}...{{/if}}。
 */
public final class MailTemplateRenderer {

    private static final Pattern IF_BLOCK = Pattern.compile(
            "\\{\\{#if\\s+([a-zA-Z0-9_]+)\\}\\}([\\s\\S]*?)\\{\\{/if\\}\\}",
            Pattern.MULTILINE);
    private static final Pattern MUSTACHE = Pattern.compile("\\{\\{([a-zA-Z0-9_]+)\\}\\}");
    private static final Pattern DOLLAR = Pattern.compile("\\$\\{([a-zA-Z0-9_]+)\\}");

    private MailTemplateRenderer() {
    }

    public static String render(String text, Map<String, String> variables) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        Map<String, String> vars = expandAliases(variables);
        String out = text;
        // 先处理条件块，再替换变量
        out = applyIfBlocks(out, vars);
        out = replaceAll(out, MUSTACHE, vars);
        out = replaceAll(out, DOLLAR, vars);
        return out;
    }

    /** 写入变量，同时补充 camelCase / UPPER_SNAKE / lower 别名，方便模板混用。 */
    public static void put(Map<String, String> target, String key, String value) {
        if (target == null || !StringUtils.hasText(key)) {
            return;
        }
        String v = value == null ? "" : value;
        target.put(key, v);
        target.put(key.toLowerCase(Locale.ROOT), v);
        String snake = toUpperSnake(key);
        target.put(snake, v);
        target.put(snake.toLowerCase(Locale.ROOT), v);
        String camel = snakeToCamel(snake);
        if (StringUtils.hasText(camel)) {
            target.put(camel, v);
        }
    }

    public static Map<String, String> expandAliases(Map<String, String> variables) {
        Map<String, String> out = new HashMap<>();
        if (variables == null) {
            return out;
        }
        for (Map.Entry<String, String> e : variables.entrySet()) {
            put(out, e.getKey(), e.getValue());
        }
        // 常见同义名
        copyAlias(out, "expireMinutes", "validityMinutes");
        copyAlias(out, "validityMinutes", "expireMinutes");
        copyAlias(out, "content", "noticeContent");
        copyAlias(out, "noticeContent", "content");
        copyAlias(out, "reply", "replyContent");
        copyAlias(out, "replyContent", "reply");
        copyAlias(out, "carrier", "shippingCarrier");
        copyAlias(out, "shippingCarrier", "carrier");
        copyAlias(out, "trackingNo", "trackingNumber");
        copyAlias(out, "trackingNumber", "trackingNo");
        return out;
    }

    private static void copyAlias(Map<String, String> map, String from, String to) {
        if (!map.containsKey(to) && map.containsKey(from)) {
            put(map, to, map.get(from));
        }
    }

    private static String applyIfBlocks(String text, Map<String, String> vars) {
        String out = text;
        // 循环以支持嵌套较浅的多块
        for (int i = 0; i < 20; i++) {
            Matcher m = IF_BLOCK.matcher(out);
            if (!m.find()) {
                break;
            }
            StringBuffer sb = new StringBuffer();
            m.reset();
            while (m.find()) {
                String key = m.group(1);
                String inner = m.group(2);
                String replacement = isTruthy(lookup(vars, key)) ? inner : "";
                m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            m.appendTail(sb);
            out = sb.toString();
        }
        return out;
    }

    private static String replaceAll(String text, Pattern pattern, Map<String, String> vars) {
        Matcher m = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            String val = lookup(vars, key);
            if (val == null) {
                val = "";
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(val));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String lookup(Map<String, String> vars, String key) {
        if (vars.containsKey(key)) {
            return vars.get(key);
        }
        String lower = key.toLowerCase(Locale.ROOT);
        if (vars.containsKey(lower)) {
            return vars.get(lower);
        }
        String snake = toUpperSnake(key);
        if (vars.containsKey(snake)) {
            return vars.get(snake);
        }
        String camel = snakeToCamel(key.contains("_") ? key : snake);
        if (vars.containsKey(camel)) {
            return vars.get(camel);
        }
        return "";
    }

    private static boolean isTruthy(String v) {
        if (!StringUtils.hasText(v)) {
            return false;
        }
        String t = v.trim();
        return !"-".equals(t) && !"null".equalsIgnoreCase(t) && !"false".equalsIgnoreCase(t);
    }

    static String toUpperSnake(String key) {
        if (!StringUtils.hasText(key)) {
            return "";
        }
        if (key.indexOf('_') >= 0) {
            return key.toUpperCase(Locale.ROOT);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                sb.append('_');
            }
            sb.append(Character.toUpperCase(c));
        }
        return sb.toString();
    }

    static String snakeToCamel(String snake) {
        if (!StringUtils.hasText(snake)) {
            return "";
        }
        String[] parts = snake.toLowerCase(Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                continue;
            }
            sb.append(Character.toUpperCase(parts[i].charAt(0)));
            if (parts[i].length() > 1) {
                sb.append(parts[i].substring(1));
            }
        }
        return sb.toString();
    }
}
