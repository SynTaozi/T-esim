package com.tesim.service;

import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.ticket.CreateTicketRequest;
import com.tesim.dto.ticket.TicketReplyRequest;
import com.tesim.dto.ticket.TicketVO;
import com.tesim.entity.Ticket;
import com.tesim.entity.TicketMessage;
import com.tesim.security.SecurityUtils;
import com.tesim.util.LocaleHelper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Map;
import java.math.BigDecimal;

public interface TicketAppService {

    TicketVO create(CreateTicketRequest req);



    PageResult<TicketVO> list(Integer page, Integer size, String status);



    TicketVO getById(Long id);



    TicketVO getByNo(String ticketNo);



    TicketVO followUp(Long id, TicketReplyRequest req);
}
