package com.tesim.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminEmailSendLogVO {
    private Long id;
    private Long accountId;
    private String purpose;
    private String bizType;
    private String bizId;
    private String toEmail;
    private String subject;
    private String body;
    private String status;
    private String errorMessage;
    private Integer attemptCount;
    private String sentAt;
    private String createdAt;
}
