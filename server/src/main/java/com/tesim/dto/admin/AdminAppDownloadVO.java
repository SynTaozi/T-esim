package com.tesim.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAppDownloadVO {
    private Long id;
    private String platform;
    private String name;
    private String titleZh;
    private String nameEn;
    private String titleEn;
    private String downloadUrl;
    private String qrImageUrl;
    private Integer sortOrder;
    private Integer status;
    private String remark;
}
