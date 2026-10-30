package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.SysEmailAccount;
import com.tesim.mapper.SysEmailAccountMapper;
import com.tesim.service.SysEmailAccountService;
import org.springframework.stereotype.Service;

@Service
public class SysEmailAccountServiceImpl extends ServiceImpl<SysEmailAccountMapper, SysEmailAccount> implements SysEmailAccountService {
}
