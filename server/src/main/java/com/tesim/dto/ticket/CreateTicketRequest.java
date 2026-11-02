package com.tesim.dto.ticket;

import lombok.Data;

@Data
public class CreateTicketRequest {
    private String type;
    private String priority;
    private Long orderId;
    private String subject;
    private String content;
}
