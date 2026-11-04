package com.tesim.mail;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.Map;

@Data
@Builder
public class MailMessage {

    /** 指定发信账号；为空则按 purpose 自动选择 */
    private Long accountId;

    /** SHIPPING / PAYMENT / SUPPORT / GENERAL */
    private String purpose;

    /** 业务类型，写入发送日志 */
    private String bizType;

    /** 业务关联 ID（订单号、工单号等） */
    private String bizId;

    private String to;

    private String subject;

    /** HTML 正文 */
    private String htmlBody;

    /** 模板 code；有值时优先渲染模板（subject/htmlBody 可作为缺省回退） */
    private String templateCode;

    /** zh / en */
    @Builder.Default
    private String locale = "zh";

    @Builder.Default
    private Map<String, String> variables = Collections.emptyMap();
}
