package com.tesim.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTutorialVO {
    private Long id;
    private Long categoryId;
    private String slug;
    private String contentType;
    private String title;
    private String titleZh;
    private String titleEn;
    private String summary;
    private String summaryEn;
    private String content;
    private String contentZh;
    private String contentEn;
    private String coverImageUrl;
    private String coverUrl;
    private String mediaUrl;
    private Integer sort;
    private Integer sortOrder;
    private Integer status;
}
