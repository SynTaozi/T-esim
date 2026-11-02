package com.tesim.dto.admin;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class AdminAppDownloadRequest {
    private String platform;
    @JsonAlias({"titleZh", "nameZh"})
    private String name;
    @JsonAlias({"titleEn"})
    private String nameEn;
    private String downloadUrl;
    private String qrImageUrl;
    private Integer sortOrder;
    private Integer status;
    private String remark;
}
