package com.tesim.dto.admin;

import lombok.Data;

@Data
public class AdminEmailTemplateRequest {
    private String name;
    private String nameEn;
    private String subject;
    private String subjectEn;
    private String body;
    private String bodyEn;
    private Boolean enabled;
    private String description;
}
