package com.tesim.dto.admin;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class AdminTutorialCategoryRequest {
    @JsonAlias({"nameZh"})
    private String name;

    private String nameEn;
    private String icon;
    private Integer sort;
    private Integer sortOrder;
    private Integer status;
}
