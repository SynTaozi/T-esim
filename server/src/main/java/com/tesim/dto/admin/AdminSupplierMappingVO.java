package com.tesim.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSupplierMappingVO {
    private Long id;
    private Long configId;
    private Long channelId;
    private Long productId;
    private Long skuId;
    private Long planId;
    private String planIdStr;
    private String externalPlanId;
    private String externalName;
    private String terminal;
    private Boolean autoDeliveryEnabled;
    private Boolean autoFulfill;
}
