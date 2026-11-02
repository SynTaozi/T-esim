package com.tesim.dto.ticket;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketMessageVO {
    private Long id;
    private String content;
    private boolean admin;
    private String senderType;
    private String createdAt;
}
