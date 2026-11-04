package com.tesim.service.impl;

import com.tesim.entity.SysEmailAccount;
import com.tesim.entity.SysEmailSendLog;
import com.tesim.service.SysEmailAccountService;
import com.tesim.service.SysEmailSendLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailSendExecutor {

    private final SysEmailSendLogService sendLogService;
    private final SysEmailAccountService emailAccountService;
    private final MailSmtpClient smtpClient;

    @Async("mailExecutor")
    public void deliverAsync(Long logId) {
        deliver(logId);
    }

    public void deliver(Long logId) {
        if (logId == null) {
            return;
        }
        SysEmailSendLog row = sendLogService.getById(logId);
        if (row == null) {
            return;
        }
        if ("SUCCESS".equalsIgnoreCase(row.getStatus())) {
            return;
        }
        int attempts = row.getAttemptCount() == null ? 0 : row.getAttemptCount();
        row.setAttemptCount(attempts + 1);
        try {
            SysEmailAccount account = row.getAccountId() == null
                    ? null
                    : emailAccountService.getById(row.getAccountId());
            if (account == null || !Boolean.TRUE.equals(account.getEnabled())) {
                throw new IllegalStateException("发信账号不存在或未启用");
            }
            smtpClient.sendHtml(account, row.getToEmail(), row.getSubject(), row.getBody());
            row.setStatus("SUCCESS");
            row.setErrorMessage(null);
            row.setSentAt(LocalDateTime.now());
            sendLogService.updateById(row);
        } catch (Exception e) {
            log.warn("邮件发送失败 logId={} to={}: {}", logId, row.getToEmail(), e.getMessage());
            row.setStatus("FAILED");
            row.setErrorMessage(trimErr(e.getMessage()));
            sendLogService.updateById(row);
        }
    }

    private static String trimErr(String msg) {
        if (msg == null) {
            return "unknown error";
        }
        return msg.length() > 900 ? msg.substring(0, 900) : msg;
    }
}
