package com.tesim.service.impl;

import com.tesim.entity.SysEmailAccount;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 按账号动态创建 SMTP Session（多发信账户，不依赖 spring.mail 单账号配置）。
 */
@Component
public class MailSmtpClient {

    public void sendHtml(SysEmailAccount account, String to, String subject, String htmlBody) throws Exception {
        if (account == null) {
            throw new IllegalArgumentException("发信账号为空");
        }
        if (!StringUtils.hasText(account.getSmtpHost()) || account.getSmtpPort() == null) {
            throw new IllegalStateException("SMTP 主机或端口未配置");
        }
        if (!StringUtils.hasText(to)) {
            throw new IllegalArgumentException("收件人为空");
        }

        int port = account.getSmtpPort();
        Properties props = new Properties();
        props.put("mail.smtp.host", account.getSmtpHost().trim());
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "20000");
        props.put("mail.smtp.writetimeout", "20000");
        if (port == 465) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", String.valueOf(port));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }

        String username = StringUtils.hasText(account.getUsername())
                ? account.getUsername().trim()
                : account.getEmailAddress();
        String password = account.getPassword() == null ? "" : account.getPassword();

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        MimeMessage message = new MimeMessage(session);
        String from = StringUtils.hasText(account.getEmailAddress())
                ? account.getEmailAddress().trim()
                : username;
        String personal = StringUtils.hasText(account.getName()) ? account.getName().trim() : from;
        message.setFrom(new InternetAddress(from, personal, StandardCharsets.UTF_8.name()));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to.trim()));
        message.setSubject(subject == null ? "" : subject, StandardCharsets.UTF_8.name());
        message.setContent(htmlBody == null ? "" : htmlBody, "text/html; charset=UTF-8");
        Transport.send(message);
    }
}
