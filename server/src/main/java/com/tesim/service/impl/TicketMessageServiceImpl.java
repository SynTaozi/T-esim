package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.TicketMessage;
import com.tesim.mapper.TicketMessageMapper;
import com.tesim.service.TicketMessageService;
import org.springframework.stereotype.Service;

@Service
public class TicketMessageServiceImpl extends ServiceImpl<TicketMessageMapper, TicketMessage> implements TicketMessageService {
}
