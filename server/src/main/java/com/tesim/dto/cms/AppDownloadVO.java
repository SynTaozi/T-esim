package com.tesim.dto.cms;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppDownloadVO {
    private Long id;
    private String platform;
    private String downloadUrl;
    private String qrImageUrl;
    private String name;
    private String remark;
}
