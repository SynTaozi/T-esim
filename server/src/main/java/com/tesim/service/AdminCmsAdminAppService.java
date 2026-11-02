package com.tesim.service;

import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.*;
import com.tesim.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface AdminCmsAdminAppService {

    List<AdminFaqCategoryVO> listFaqCategories();



    AdminFaqCategoryVO createFaqCategory(AdminFaqCategoryRequest req);



    AdminFaqCategoryVO updateFaqCategory(Long id, AdminFaqCategoryRequest req);



    void deleteFaqCategory(Long id);



    List<AdminFaqVO> listFaqs(Long categoryId);



    AdminFaqVO createFaq(AdminFaqRequest req);



    AdminFaqVO updateFaq(Long id, AdminFaqRequest req);



    void deleteFaq(Long id);



    List<AdminTutorialCategoryVO> listTutorialCategories();



    AdminTutorialCategoryVO createTutorialCategory(AdminTutorialCategoryRequest req);



    AdminTutorialCategoryVO updateTutorialCategory(Long id, AdminTutorialCategoryRequest req);



    void deleteTutorialCategory(Long id);



    List<AdminTutorialVO> listTutorials(Long categoryId, String contentType);



    AdminTutorialVO createTutorial(AdminTutorialRequest req);



    AdminTutorialVO updateTutorial(Long id, AdminTutorialRequest req);



    void deleteTutorial(Long id);



    List<AdminAppDownloadVO> listAppDownloads();



    AdminAppDownloadVO getAppDownload(Long id);



    AdminAppDownloadVO createAppDownload(AdminAppDownloadRequest req);



    AdminAppDownloadVO updateAppDownload(Long id, AdminAppDownloadRequest req);



    void deleteAppDownload(Long id);



    List<AdminPaymentMethodVO> listPaymentMethods(Boolean enabledOnly);



    AdminPaymentMethodVO savePaymentMethod(AdminPaymentMethodRequest req);



    void updatePaymentEnabled(Long id, Boolean enabled);



    void deletePaymentMethod(Long id);



    List<AdminCouponVO> listCoupons();



    AdminCouponVO createCoupon(AdminCouponRequest req);



    AdminCouponVO updateCoupon(Long id, AdminCouponRequest req);



    AdminCouponVO disableCoupon(Long id);



    PageResult<AdminCouponUsageVO> pageCouponUsages(Long couponId, Integer page, Integer size);



    AdminSiteDisplayRequest getSiteDisplay();



    AdminSiteDisplayRequest updateSiteDisplay(AdminSiteDisplayRequest req);



    Map<String, String> getTheme();



    Map<String, String> setTheme(AdminThemeRequest req);


}
