package com.tesim.dto.admin;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class AdminTutorialRequest {
    private Long categoryId;
    private String slug;
    private String contentType;
    @JsonAlias({"titleZh"})
    private String title;
    private String titleEn;
    private String summary;
    private String summaryEn;
    @JsonAlias({"contentZh"})
    private String content;
    private String contentEn;
    private String coverImageUrl;
    private String coverUrl;
    private String mediaUrl;
    private Integer sort;
    private Integer sortOrder;
    private Integer status;
}
