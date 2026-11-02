package com.tesim.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFaqCategoryVO {
    private Long id;
    private String name;
    private String nameZh;
    private String nameEn;
    private Integer sort;
    private Integer sortOrder;
    private Integer status;
}
