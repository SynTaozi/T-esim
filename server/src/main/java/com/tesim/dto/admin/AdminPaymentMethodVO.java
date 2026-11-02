package com.tesim.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPaymentMethodVO {
    private Long id;
    private String code;
    private String name;
    /** Alias for admin UI (中文名) */
    private String nameZh;
    private String nameEn;
    private String description;
    private String descriptionEn;
    private String icon;
    private Boolean enabled;
    private Integer sortOrder;
    private String extraJson;
}
