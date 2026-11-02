package com.tesim.dto.admin;

import lombok.Data;

@Data
public class AdminSupplierConfigRequest {
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
