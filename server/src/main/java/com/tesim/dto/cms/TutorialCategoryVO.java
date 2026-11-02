package com.tesim.dto.cms;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TutorialCategoryVO {
    private Long id;
    private String name;
    private String icon;
    private Integer sortOrder;
}
