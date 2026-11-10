package com.tesim.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesim.entity.SysDingtalkConfig;
import com.tesim.entity.SysDingtalkNotifyLog;
import com.tesim.service.SysDingtalkConfigService;
import com.tesim.service.SysDingtalkNotifyLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DingtalkSendExecutor {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private final SysDingtalkConfigService configService;
    private final SysDingtalkNotifyLogService logService;

    @Async("mailExecutor")
    public void sendMarkdownAsync(String eventType, String title, String markdownText) {
        SysDingtalkConfig config = configService.getOne(new LambdaQueryWrapper<SysDingtalkConfig>()
                .eq(SysDingtalkConfig::getEnabled, true)
                .orderByDesc(SysDingtalkConfig::getId)
                .last("LIMIT 1"));
        if (config == null || !StringUtils.hasText(config.getWebhookUrl())) {
            log.warn("[DingTalk] 无启用配置，跳过 type={}", eventType);
            saveLog(null, eventType, title, markdownText, "FAILED", "未找到启用的钉钉配置");
            return;
        }
        try {
            postMarkdown(config.getWebhookUrl().trim(), title, markdownText);
            log.info("[DingTalk] 发送成功 type={} title={}", eventType, title);
            saveLog(config.getId(), eventType, title, markdownText, "SUCCESS", null);
        } catch (Exception e) {
            log.warn("[DingTalk] 发送失败 type={}: {}", eventType, e.getMessage());
            saveLog(config.getId(), eventType, title, markdownText, "FAILED", trimErr(e.getMessage()));
        }
    }

    public void sendTextSync(SysDingtalkConfig config, String eventType, String title, String content) throws Exception {
        postText(config.getWebhookUrl().trim(), content);
        saveLog(config.getId(), eventType, title, content, "SUCCESS", null);
    }

    public void saveFailure(Long configId, String eventType, String title, String content, String error) {
        saveLog(configId, eventType, title, content, "FAILED", trimErr(error));
    }

    private void postMarkdown(String webhookUrl, String title, String text) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("msgtype", "markdown");
        Map<String, String> markdown = new HashMap<>();
        markdown.put("title", title == null ? "Tesim" : title);
        markdown.put("text", text == null ? "" : text);
        body.put("markdown", markdown);
        postJson(webhookUrl, body);
    }

    private void postText(String webhookUrl, String content) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("msgtype", "text");
        Map<String, String> text = new HashMap<>();
        text.put("content", content == null ? "" : content);
        body.put("text", text);
        postJson(webhookUrl, body);
    }

    private void postJson(String webhookUrl, Map<String, Object> body) throws Exception {
        String json = MAPPER.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("HTTP " + response.statusCode() + ": " + abbreviate(response.body()));
        }
        String resp = response.body() == null ? "" : response.body();
        if (resp.contains("\"errcode\"") && !resp.contains("\"errcode\":0") && !resp.contains("\"errcode\": 0")) {
            throw new IllegalStateException(abbreviate(resp));
        }
    }

    private void saveLog(Long configId, String eventType, String title, String content, String status, String error) {
        try {
            SysDingtalkNotifyLog row = new SysDingtalkNotifyLog();
            row.setConfigId(configId);
            row.setEventType(eventType);
            row.setTitle(title);
            row.setContent(content);
            row.setStatus(status);
            row.setErrorMessage(error);
            row.setCreatedAt(LocalDateTime.now());
            logService.save(row);
        } catch (Exception e) {
            log.warn("[DingTalk] 写日志失败: {}", e.getMessage());
        }
    }

    private static String trimErr(String msg) {
        if (msg == null) {
            return "unknown";
        }
        return msg.length() > 900 ? msg.substring(0, 900) : msg;
    }

    private static String abbreviate(String s) {
        if (s == null) {
            return "";
        }
        return s.length() > 300 ? s.substring(0, 300) : s;
    }
}
