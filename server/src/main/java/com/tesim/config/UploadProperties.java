package com.tesim.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tesim.upload")
public class UploadProperties {

    /** 本地落盘目录（相对进程工作目录或绝对路径） */
    private String dir = "uploads";

    /** 对外访问前缀（配合 context-path=/api → /api/uploads/...） */
    private String publicPath = "/uploads";

    private long maxBytes = 5L * 1024 * 1024;
}
