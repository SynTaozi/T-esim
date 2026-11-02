package com.tesim.service;

import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.AdminMemberVO;
import com.tesim.entity.Member;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.math.BigDecimal;

public interface AdminMemberAppService {

    PageResult<AdminMemberVO> page(Integer page, Integer size, String keyword, String status);



    AdminMemberVO get(Long id);



    AdminMemberVO updateStatus(Long id, String status);


}
