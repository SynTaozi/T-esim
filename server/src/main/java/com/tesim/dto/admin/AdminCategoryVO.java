package com.tesim.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCategoryVO {
    private Long id;
    private Long parentId;
    private Long categoryId;
    private String categoryName;
    private String code;
    private String icon;
    private String name;
    private String nameZh;
    private String nameEn;
    private String description;
    private String descriptionZh;
    private Integer sortOrder;
    private Integer status;
    private Boolean showOnFront;
}
