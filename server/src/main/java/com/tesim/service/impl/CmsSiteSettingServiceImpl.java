package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.CmsSiteSetting;
import com.tesim.mapper.CmsSiteSettingMapper;
import com.tesim.service.CmsSiteSettingService;
import org.springframework.stereotype.Service;

@Service
public class CmsSiteSettingServiceImpl extends ServiceImpl<CmsSiteSettingMapper, CmsSiteSetting> implements CmsSiteSettingService {
}
