package com.tesim.dto.admin;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.List;

@Data
public class AdminTicketReplyRequest {
    /** 回复正文（管理端主字段） */
    private String reply;
    /** 兼容前端误传 content */
    private String content;
    private String repliedBy;
    /** 已上传图片 URL */
    private List<String> imageUrls;

    public String resolveReply() {
        if (StringUtils.hasText(reply)) {
            return reply.trim();
        }
        if (StringUtils.hasText(content)) {
            return content.trim();
        }
        return null;
    }
}
