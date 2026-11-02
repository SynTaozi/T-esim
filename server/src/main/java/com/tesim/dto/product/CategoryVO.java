package com.tesim.dto.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryVO {
    private Long id;
    private String code;
    private Long parentId;
    private String name;
    private String description;
    private String icon;
    private Integer sortOrder;
}
