package com.tesim.dto.admin;

import lombok.Data;

@Data
public class AdminCategorySaveRequest {
    private Long parentId;
    private String code;
    private String icon;
    private Integer sortOrder;
    private Integer status;
    private Boolean showOnFront;
    private String nameZh;
    private String nameEn;
    private String descriptionZh;
    private String descriptionEn;
}
