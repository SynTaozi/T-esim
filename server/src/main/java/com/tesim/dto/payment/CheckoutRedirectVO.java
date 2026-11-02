package com.tesim.dto.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckoutRedirectVO {
    private String url;
    private String checkoutUrl;
    private String normalUrl;
    private String sessionId;
    private String paymentRequestId;
    private String orderNo;
    private Boolean stub;
}
