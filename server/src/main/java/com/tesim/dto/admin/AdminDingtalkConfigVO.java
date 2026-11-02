package com.tesim.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDingtalkConfigVO {
    private Long id;
    private String name;
    private String webhookUrl;
    private Boolean enabled;
    private String description;
}
