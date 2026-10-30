package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.SysTheme;
import com.tesim.mapper.SysThemeMapper;
import com.tesim.service.SysThemeService;
import org.springframework.stereotype.Service;

@Service
public class SysThemeServiceImpl extends ServiceImpl<SysThemeMapper, SysTheme> implements SysThemeService {
}
