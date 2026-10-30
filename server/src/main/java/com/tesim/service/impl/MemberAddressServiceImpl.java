package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.MemberAddress;
import com.tesim.mapper.MemberAddressMapper;
import com.tesim.service.MemberAddressService;
import org.springframework.stereotype.Service;

@Service
public class MemberAddressServiceImpl extends ServiceImpl<MemberAddressMapper, MemberAddress> implements MemberAddressService {
}
