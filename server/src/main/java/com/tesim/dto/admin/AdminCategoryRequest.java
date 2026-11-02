package com.tesim.dto.admin;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class AdminCategoryRequest {
    private Long parentId;
    private Long categoryId;
    private String code;
    private String icon;
    @JsonAlias({"nameZh"})
    private String name;
    private String nameEn;
    @JsonAlias({"descriptionZh"})
    private String description;
    private Integer sortOrder;
    private Integer status;
    private Boolean showOnFront;
}
