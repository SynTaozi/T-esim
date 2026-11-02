package com.tesim.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTutorialCategoryVO {
    private Long id;
    private String name;
    private String nameEn;
    private String icon;
    private Integer sort;
    private Integer sortOrder;
    private Integer status;
}
