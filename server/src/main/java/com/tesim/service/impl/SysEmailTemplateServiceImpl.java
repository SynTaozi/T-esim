package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.SysEmailTemplate;
import com.tesim.mapper.SysEmailTemplateMapper;
import com.tesim.service.SysEmailTemplateService;
import org.springframework.stereotype.Service;

@Service
public class SysEmailTemplateServiceImpl extends ServiceImpl<SysEmailTemplateMapper, SysEmailTemplate> implements SysEmailTemplateService {
}
