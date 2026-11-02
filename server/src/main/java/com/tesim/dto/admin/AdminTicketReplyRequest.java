package com.tesim.dto.admin;

import lombok.Data;

@Data
public class AdminTicketReplyRequest {
    private String reply;
    private String repliedBy;
}
