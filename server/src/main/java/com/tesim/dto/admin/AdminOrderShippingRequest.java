package com.tesim.dto.admin;

import lombok.Data;

@Data
public class AdminOrderShippingRequest {
    private String shippingPhone;
    private String trackingNumber;
    private String shippingCarrier;
    private String trackingQrCode;
    private String shippingRemark;
}
