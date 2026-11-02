package com.tesim.dto.cms;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TutorialVO {
    private Long id;
    private Long categoryId;
    private String slug;
    private String contentType;
    private String coverUrl;
    private String mediaUrl;
    private Integer sortOrder;
    private String title;
    private String summary;
    /** Present for detail; null/omitted for list responses. */
    private String content;
}
