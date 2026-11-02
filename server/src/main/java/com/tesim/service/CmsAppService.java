package com.tesim.service;

import com.tesim.common.BizException;
import com.tesim.dto.cms.*;
import com.tesim.entity.*;
import com.tesim.util.LocaleHelper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Map;

public interface CmsAppService {

    List<FaqCategoryVO> faqCategories();




    List<FaqVO> faqs(Long categoryId);




    FaqVO faqById(Long id);




    FaqCategoryVO faqCategoryById(Long id);




    List<TutorialCategoryVO> tutorialCategories();




    List<TutorialVO> tutorials(Long categoryId);




    TutorialVO tutorialBySlug(String slug);




    TutorialOverviewVO tutorialOverview();




    List<AppDownloadVO> appDownloads();




    SiteDisplayVO siteDisplay();




    ThemeVO currentTheme();




    List<PaymentMethodVO> enabledPaymentMethods();


}
