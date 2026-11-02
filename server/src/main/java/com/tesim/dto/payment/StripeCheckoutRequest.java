package com.tesim.dto.payment;

import lombok.Data;

@Data
public class StripeCheckoutRequest {
    private String orderNo;
    private Long orderId;
    private String paymentMethod;
    private String email;
}
