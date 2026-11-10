package com.tesim.service;

import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.AdminEmailAccountRequest;
import com.tesim.dto.admin.AdminEmailAccountVO;
import com.tesim.dto.admin.AdminEmailSendLogVO;
import com.tesim.dto.admin.AdminEmailTemplateRequest;
import com.tesim.dto.admin.AdminEmailTemplateVO;
import com.tesim.entity.I18nSysEmailTemplate;
import com.tesim.entity.SalesOrderNotice;
import com.tesim.entity.SysEmailAccount;
import com.tesim.entity.SysEmailTemplate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

public interface AdminEmailAppService {

    List<AdminEmailAccountVO> listAccounts();



    AdminEmailAccountVO getAccount(Long id);



    AdminEmailAccountVO createAccount(AdminEmailAccountRequest req);



    AdminEmailAccountVO updateAccount(Long id, AdminEmailAccountRequest req);



    void deleteAccount(Long id);



    Map<String, Object> testAccount(Long id);

    List<AdminEmailTemplateVO> listTemplates();



    AdminEmailTemplateVO getTemplate(Long id);



    AdminEmailTemplateVO updateTemplate(Long id, AdminEmailTemplateRequest req);



    AdminEmailTemplateVO updateTemplateEnabled(Long id, Boolean enabled);



    Map<String, Object> previewTemplate(Long id);

    PageResult<AdminEmailSendLogVO> pageSendLogs(String status, String bizType, String toEmail,
                                                 Integer page, Integer size);
}
