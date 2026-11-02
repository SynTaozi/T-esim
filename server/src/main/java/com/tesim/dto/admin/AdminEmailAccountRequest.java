package com.tesim.dto.admin;

import lombok.Data;

@Data
public class AdminEmailAccountRequest {
    private String name;
    private String emailAddress;
    private String username;
    private String password;
    private String smtpHost;
    private Integer smtpPort;
    private String imapHost;
    private Integer imapPort;
    private String purpose;
    private Boolean enabled;
    private String description;
}
