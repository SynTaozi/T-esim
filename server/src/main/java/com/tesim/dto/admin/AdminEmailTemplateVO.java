package com.tesim.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminEmailTemplateVO {
    private Long id;
    private String code;
    private String name;
    private String nameEn;
    private String subject;
    private String subjectEn;
    private String body;
    private String bodyEn;
    private Boolean enabled;
    private String description;
}
