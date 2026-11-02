package com.tesim.dto.payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateUsdtPaymentRequest {
    private String orderNo;
    private Long orderId;
    private BigDecimal amount;
    private String productName;
    private String remark;
}
