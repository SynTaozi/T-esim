package com.tesim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * T-esim 后端启动类（默认 profile=dev）
 * 排除默认 MailSender：发信走多账号动态 SMTP（MailSmtpClient），不依赖 spring.mail.*
 */
@SpringBootApplication(exclude = MailSenderAutoConfiguration.class)
@EnableScheduling
public class TEsimApplication {

    public static void main(String[] args) {
        SpringApplication.run(TEsimApplication.class, args);
    }
}
