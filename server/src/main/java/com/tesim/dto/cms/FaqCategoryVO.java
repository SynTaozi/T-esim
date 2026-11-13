package com.tesim.dto.cms;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FaqCategoryVO {
    private Long id;
    private String name;
    private Integer sortOrder;
    /** 公开接口仅返回上架项；保留字段兼容前台过滤逻辑 */
    private Integer status;
}
