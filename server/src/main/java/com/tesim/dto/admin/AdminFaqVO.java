package com.tesim.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFaqVO {
    private Long id;
    private Long categoryId;
    private String question;
    private String questionZh;
    private String questionEn;
    private String answer;
    private String answerZh;
    private String answerEn;
    private Integer sort;
    private Integer sortOrder;
    private Integer status;
}
