package com.tesim.service;

import com.tesim.common.BizException;
import com.tesim.dto.admin.AdminPaymentMethodRequest;
import com.tesim.dto.admin.AdminPaymentMethodVO;
import com.tesim.dto.admin.AdminSiteDisplayRequest;
import com.tesim.dto.admin.AdminThemeRequest;
import com.tesim.dto.cms.SiteDisplayVO;
import com.tesim.dto.cms.ThemeVO;
import com.tesim.entity.CmsSiteSetting;
import com.tesim.entity.I18nPaymentChannel;
import com.tesim.entity.PaymentChannel;
import com.tesim.entity.SysTheme;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface AdminConfigAppService {

    List<AdminPaymentMethodVO> paymentMethods(Boolean enabledOnly);



    AdminPaymentMethodVO savePaymentMethod(AdminPaymentMethodRequest req);



    AdminPaymentMethodVO updatePaymentMethod(Long id, AdminPaymentMethodRequest req);



    AdminPaymentMethodVO updatePaymentEnabled(Long id, Boolean enabled);



    void deletePaymentMethod(Long id);



    SiteDisplayVO siteDisplay();



    SiteDisplayVO updateSiteDisplay(AdminSiteDisplayRequest req);



    ThemeVO currentTheme();



    ThemeVO setTheme(AdminThemeRequest req);


}
