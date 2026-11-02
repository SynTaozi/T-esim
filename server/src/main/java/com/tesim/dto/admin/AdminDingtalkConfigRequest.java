package com.tesim.dto.admin;

import lombok.Data;

@Data
public class AdminDingtalkConfigRequest {
    private String name;
    private String webhookUrl;
    private Boolean enabled;
    private String description;
}
