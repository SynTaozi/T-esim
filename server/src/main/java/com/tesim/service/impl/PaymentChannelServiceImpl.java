package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.PaymentChannel;
import com.tesim.mapper.PaymentChannelMapper;
import com.tesim.service.PaymentChannelService;
import org.springframework.stereotype.Service;

@Service
public class PaymentChannelServiceImpl extends ServiceImpl<PaymentChannelMapper, PaymentChannel> implements PaymentChannelService {
}
