package com.tesim.dto.payment;

import lombok.Data;

@Data
public class UsdtCallbackRequest {
    private String paymentId;
    private String transactionId;
}
