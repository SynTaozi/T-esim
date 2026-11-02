package com.tesim.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripeConfirmVO {
    private Boolean success;
    private Boolean paid;
    private String orderNo;
    private String sessionId;
    private String paymentStatus;
    private String sessionStatus;
    /** PAID / PENDING */
    private String status;
}
