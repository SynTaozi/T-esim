package com.tesim.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDingtalkLogVO {
    private Long id;
    private Long configId;
    private String eventType;
    private String type;
    private String title;
    private String content;
    private String status;
    private String errorMessage;
    private String createdAt;
}
