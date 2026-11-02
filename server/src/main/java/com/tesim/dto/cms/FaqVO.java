package com.tesim.dto.cms;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FaqVO {
    private Long id;
    private Long categoryId;
    private String question;
    private String answer;
    private Integer sortOrder;
}
