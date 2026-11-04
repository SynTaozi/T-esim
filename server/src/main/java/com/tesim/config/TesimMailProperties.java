package com.tesim.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tesim.mail")
public class TesimMailProperties {

    /** 开发联调时是否在发码接口响应中带回验证码 */
    private boolean exposeDevCode = false;

    /** 未配置业务用途账号时，是否回退到 GENERAL / 任意启用账号 */
    private boolean fallbackAccount = true;
}
