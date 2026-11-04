package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.SysEmailSendLog;
import com.tesim.mapper.SysEmailSendLogMapper;
import com.tesim.service.SysEmailSendLogService;
import org.springframework.stereotype.Service;

@Service
public class SysEmailSendLogServiceImpl extends ServiceImpl<SysEmailSendLogMapper, SysEmailSendLog>
        implements SysEmailSendLogService {
}
