package com.tesim.dto.admin;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class AdminFaqRequest {
    private Long categoryId;

    @JsonAlias({"questionZh"})
    private String question;

    private String questionEn;

    @JsonAlias({"answerZh"})
    private String answer;

    private String answerEn;

    private Integer sort;
    private Integer sortOrder;
    private Integer status;
}
