package com.tesim.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSupplierConfigVO {
    private Long id;
    private String code;
    private String name;
    private String testUsername;
    private String testApiKey;
    private String prodUsername;
    private String prodApiKey;
    private String baseUrl;
    private String environment;
    private String callbackUrl;
    private Boolean enabled;
    private String description;
    private String credentialJson;
}
