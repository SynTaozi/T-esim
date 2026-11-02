package com.tesim.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentOrderVO {
    private String paymentId;
    private String orderNo;
    private Long orderId;
    private BigDecimal amount;
    private BigDecimal usdtAmount;
    private String usdtAddress;
    private String status;
    private Long expireTime;
    private String transactionId;
    private Long paidTime;
}
