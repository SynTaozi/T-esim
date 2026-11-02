package com.tesim.dto.cms;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentMethodVO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String icon;
    private Integer sortOrder;
}
