package com.tesim.dto.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileVO {
    /** 相对站点可访问的 URL，例如 /api/uploads/tickets/xxx.jpg */
    private String url;
    private String name;
    private long size;
}
