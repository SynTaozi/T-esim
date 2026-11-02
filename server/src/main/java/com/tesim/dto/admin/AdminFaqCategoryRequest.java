package com.tesim.dto.admin;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class AdminFaqCategoryRequest {
    @JsonAlias({"nameZh"})
    private String name;

    private String nameEn;

    private Integer sort;
    private Integer sortOrder;
    private Integer status;
}
