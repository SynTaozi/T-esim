package com.tesim.dto.cms;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TutorialOverviewVO {
    private List<TutorialCategoryVO> categories;
    private List<TutorialVO> tutorials;
}
