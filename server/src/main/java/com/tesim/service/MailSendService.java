package com.tesim.service;

import com.tesim.mail.MailMessage;

/**
 * 公共邮件发送入口：业务方只构造 {@link MailMessage}，由本服务负责选账号、写日志、异步投递。
 */
public interface MailSendService {

    /**
     * 异步发送：先落 PENDING 日志，事务提交后投递 SMTP。
     *
     * @return 发送日志 ID
     */
    Long sendAsync(MailMessage message);

    /**
     * 同步发送（测试信、需要立刻知道结果的场景）。
     *
     * @return 发送日志 ID
     */
    Long sendSync(MailMessage message);
}
