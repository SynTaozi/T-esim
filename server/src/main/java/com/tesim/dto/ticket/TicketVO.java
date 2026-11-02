package com.tesim.dto.ticket;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TicketVO {
    private Long id;
    private String ticketNo;
    private Long userId;
    private Long orderId;
    private String type;
    private String category;
    private String subject;
    private String priority;
    private String status;
    private String content;
    private String createdAt;
    private String updatedAt;
    private List<TicketMessageVO> replies;
}
