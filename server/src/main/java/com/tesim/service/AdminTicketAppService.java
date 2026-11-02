package com.tesim.service;

import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.AdminTicketReplyRequest;
import com.tesim.dto.ticket.TicketMessageVO;
import com.tesim.dto.ticket.TicketVO;
import com.tesim.entity.Member;
import com.tesim.entity.Ticket;
import com.tesim.entity.TicketMessage;
import com.tesim.security.SecurityUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

public interface AdminTicketAppService {

    PageResult<TicketVO> page(Integer page, Integer size, String status);



    TicketVO get(Long id);



    TicketVO reply(Long id, AdminTicketReplyRequest req);



    TicketVO close(Long id);


}
