package com.tesim.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tesim.jwt")
public class JwtProperties {
    private String secret = "tesim-dev-jwt-secret-change-me-32bytes-min";
    private long expireHours = 168;
}
