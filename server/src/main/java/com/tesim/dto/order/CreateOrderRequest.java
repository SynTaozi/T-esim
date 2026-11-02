package com.tesim.dto.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderRequest {
    private String email;
    private String shippingAddress;
    private String remark;
    private String customerRemark;
    private String paymentMethod;
    private Long productId;
    private Long skuId;
    private String orderType;
    private Long renewalOrderId;
    private Long phoneNumberId;
    private Integer periodValue;
    private String periodUnit;
    private String productName;
    private BigDecimal totalAmount;
    private String language;
    private String couponCode;
}
