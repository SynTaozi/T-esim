package com.tesim.dto.admin;

import lombok.Data;

@Data
public class AdminSupplierMappingRequest {
    private Long configId;
    private Long channelId;
    private Long productId;
    private Long skuId;
    private Long planId;
    private String externalPlanId;
    private String externalName;
    private String terminal;
    private Boolean autoDeliveryEnabled;
    private Boolean autoFulfill;
    private String extraJson;
}
