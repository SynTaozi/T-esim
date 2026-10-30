package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.MemberSubscription;
import com.tesim.mapper.MemberSubscriptionMapper;
import com.tesim.service.MemberSubscriptionService;
import org.springframework.stereotype.Service;

@Service
public class MemberSubscriptionServiceImpl extends ServiceImpl<MemberSubscriptionMapper, MemberSubscription> implements MemberSubscriptionService {
}
