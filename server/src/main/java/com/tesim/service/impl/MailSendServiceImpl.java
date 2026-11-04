package com.tesim.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tesim.common.BizException;
import com.tesim.config.TesimMailProperties;
import com.tesim.entity.I18nSysEmailTemplate;
import com.tesim.entity.SysEmailAccount;
import com.tesim.entity.SysEmailSendLog;
import com.tesim.entity.SysEmailTemplate;
import com.tesim.mail.MailMessage;
import com.tesim.mail.MailPurposes;
import com.tesim.mail.MailTemplateRenderer;
import com.tesim.service.I18nSysEmailTemplateService;
import com.tesim.service.MailSendService;
import com.tesim.service.SysEmailAccountService;
import com.tesim.service.SysEmailSendLogService;
import com.tesim.service.SysEmailTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailSendServiceImpl implements MailSendService {

    private final SysEmailAccountService emailAccountService;
    private final SysEmailSendLogService sendLogService;
    private final SysEmailTemplateService emailTemplateService;
    private final I18nSysEmailTemplateService i18nEmailTemplateService;
    private final MailSendExecutor mailSendExecutor;
    private final TesimMailProperties mailProperties;

    @Override
    public Long sendAsync(MailMessage message) {
        SysEmailSendLog row = prepareLog(message);
        sendLogService.save(row);
        scheduleDeliver(row.getId());
        return row.getId();
    }

    @Override
    public Long sendSync(MailMessage message) {
        SysEmailSendLog row = prepareLog(message);
        sendLogService.save(row);
        mailSendExecutor.deliver(row.getId());
        SysEmailSendLog latest = sendLogService.getById(row.getId());
        if (latest != null && "FAILED".equalsIgnoreCase(latest.getStatus())) {
            throw new BizException("邮件发送失败：" + (latest.getErrorMessage() == null ? "" : latest.getErrorMessage()));
        }
        return row.getId();
    }

    private SysEmailSendLog prepareLog(MailMessage message) {
        if (message == null || !StringUtils.hasText(message.getTo())) {
            throw new BizException("收件人邮箱不能为空");
        }
        String purpose = StringUtils.hasText(message.getPurpose())
                ? message.getPurpose().trim().toUpperCase()
                : MailPurposes.GENERAL;
        String bizType = StringUtils.hasText(message.getBizType())
                ? message.getBizType().trim()
                : "GENERAL";

        RenderedMail rendered = render(message);
        SysEmailAccount account = resolveAccount(message.getAccountId(), purpose);

        SysEmailSendLog row = new SysEmailSendLog();
        row.setAccountId(account.getId());
        row.setPurpose(purpose);
        row.setBizType(bizType);
        row.setBizId(trimTo(message.getBizId(), 64));
        row.setToEmail(message.getTo().trim().toLowerCase());
        row.setSubject(trimTo(rendered.subject(), 255));
        row.setBody(rendered.body());
        row.setStatus("PENDING");
        row.setAttemptCount(0);
        row.setCreatedAt(LocalDateTime.now());
        return row;
    }

    private void scheduleDeliver(Long logId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    mailSendExecutor.deliverAsync(logId);
                }
            });
        } else {
            mailSendExecutor.deliverAsync(logId);
        }
    }

    private SysEmailAccount resolveAccount(Long accountId, String purpose) {
        if (accountId != null) {
            SysEmailAccount a = emailAccountService.getById(accountId);
            if (a == null || !Boolean.TRUE.equals(a.getEnabled())) {
                throw new BizException("发信账号不存在或未启用");
            }
            return a;
        }
        SysEmailAccount byPurpose = emailAccountService.getOne(new LambdaQueryWrapper<SysEmailAccount>()
                .eq(SysEmailAccount::getPurpose, purpose)
                .eq(SysEmailAccount::getEnabled, true)
                .orderByDesc(SysEmailAccount::getId)
                .last("LIMIT 1"));
        if (byPurpose != null) {
            return byPurpose;
        }
        if (!mailProperties.isFallbackAccount()) {
            throw new BizException("未配置用途为 " + purpose + " 的发信邮箱");
        }
        if (!MailPurposes.GENERAL.equals(purpose)) {
            SysEmailAccount general = emailAccountService.getOne(new LambdaQueryWrapper<SysEmailAccount>()
                    .eq(SysEmailAccount::getPurpose, MailPurposes.GENERAL)
                    .eq(SysEmailAccount::getEnabled, true)
                    .orderByDesc(SysEmailAccount::getId)
                    .last("LIMIT 1"));
            if (general != null) {
                return general;
            }
        }
        SysEmailAccount any = emailAccountService.getOne(new LambdaQueryWrapper<SysEmailAccount>()
                .eq(SysEmailAccount::getEnabled, true)
                .orderByDesc(SysEmailAccount::getId)
                .last("LIMIT 1"));
        if (any == null) {
            throw new BizException("未配置可用发信邮箱，请先在后台添加并启用邮件账户");
        }
        return any;
    }

    private RenderedMail render(MailMessage message) {
        String subject = message.getSubject();
        String body = message.getHtmlBody();
        if (StringUtils.hasText(message.getTemplateCode())) {
            SysEmailTemplate tpl = emailTemplateService.getOne(new LambdaQueryWrapper<SysEmailTemplate>()
                    .eq(SysEmailTemplate::getCode, message.getTemplateCode().trim())
                    .last("LIMIT 1"));
            if (tpl != null && Boolean.TRUE.equals(tpl.getEnabled())) {
                String locale = normalizeLocale(message.getLocale());
                I18nSysEmailTemplate i18n = loadI18n(tpl.getId(), locale);
                if (i18n == null && !"zh".equals(locale)) {
                    i18n = loadI18n(tpl.getId(), "zh");
                }
                if (i18n == null && !"en".equals(locale)) {
                    i18n = loadI18n(tpl.getId(), "en");
                }
                if (i18n != null) {
                    if (StringUtils.hasText(i18n.getSubject())) {
                        subject = i18n.getSubject();
                    }
                    if (StringUtils.hasText(i18n.getBody())) {
                        body = i18n.getBody();
                    }
                }
            } else {
                log.debug("邮件模板不可用 code={}", message.getTemplateCode());
            }
        }
        Map<String, String> vars = message.getVariables() == null ? Map.of() : message.getVariables();
        subject = MailTemplateRenderer.render(subject, vars);
        body = MailTemplateRenderer.render(body, vars);
        if (!StringUtils.hasText(subject)) {
            subject = message.getBizType() == null ? "Tesim notification" : message.getBizType();
        }
        if (!StringUtils.hasText(body)) {
            body = "<p>" + escape(subject) + "</p>";
        }
        return new RenderedMail(subject, body);
    }

    private I18nSysEmailTemplate loadI18n(Long templateId, String locale) {
        return i18nEmailTemplateService.getOne(new LambdaQueryWrapper<I18nSysEmailTemplate>()
                .eq(I18nSysEmailTemplate::getTemplateId, templateId)
                .eq(I18nSysEmailTemplate::getLocale, locale)
                .last("LIMIT 1"));
    }

    private static String normalizeLocale(String locale) {
        if (!StringUtils.hasText(locale)) {
            return "zh";
        }
        String v = locale.trim().toLowerCase();
        if (v.startsWith("en")) {
            return "en";
        }
        return "zh";
    }

    private static String trimTo(String v, int max) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.length() > max ? t.substring(0, max) : t;
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private record RenderedMail(String subject, String body) {
    }
}
