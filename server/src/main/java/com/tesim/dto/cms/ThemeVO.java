package com.tesim.dto.cms;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThemeVO {
    private String themeId;
    private String themeName;
    private String code;
}
