package com.tesim.dto.admin;

import lombok.Data;

@Data
public class AdminTutorialCategoryRequest {
    private String name;
    private String nameEn;
    private String icon;
    private Integer sort;
    private Integer sortOrder;
    private Integer status;
}
