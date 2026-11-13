package com.tesim.dto.order;

import com.fasterxml.jackson.annotation.JsonAlias;
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
    @JsonAlias({"pricingTierId", "tierId"})
    private Long skuId;
    private String orderType;
    private Long renewalOrderId;
    private Long phoneNumberId;
    @JsonAlias({"selectedMonths"})
    private Integer periodValue;
    private String periodUnit;
    private String productName;
    private BigDecimal totalAmount;
    private String language;
    private String couponCode;
    /** esim | physical */
    private String deliveryMode;
}
