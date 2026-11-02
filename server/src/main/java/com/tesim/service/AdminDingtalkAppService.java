package com.tesim.service;

import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.AdminDingtalkConfigRequest;
import com.tesim.dto.admin.AdminDingtalkConfigVO;
import com.tesim.dto.admin.AdminDingtalkLogVO;
import com.tesim.entity.SysDingtalkConfig;
import com.tesim.entity.SysDingtalkNotifyLog;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

public interface AdminDingtalkAppService {

    List<AdminDingtalkConfigVO> listConfigs();



    AdminDingtalkConfigVO getConfig(Long id);



    AdminDingtalkConfigVO createConfig(AdminDingtalkConfigRequest req);



    AdminDingtalkConfigVO updateConfig(Long id, AdminDingtalkConfigRequest req);



    void deleteConfig(Long id);



    Map<String, Object> testSend(Long id);



    PageResult<AdminDingtalkLogVO> pageLogs(String type, Integer page, Integer size);


}
