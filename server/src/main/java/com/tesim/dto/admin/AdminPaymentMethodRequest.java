package com.tesim.dto.admin;

import lombok.Data;

@Data
public class AdminPaymentMethodRequest {
    private Long id;
    private String code;
    private String name;
    /** Admin UI may send nameZh instead of name */
    private String nameZh;
    private String nameEn;
    private String description;
    private String descriptionEn;
    private String icon;
    private Boolean enabled;
    private Integer sortOrder;
    private String extraJson;
}
