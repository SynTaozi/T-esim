package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.CmsTutorial;
import com.tesim.mapper.CmsTutorialMapper;
import com.tesim.service.CmsTutorialService;
import org.springframework.stereotype.Service;

@Service
public class CmsTutorialServiceImpl extends ServiceImpl<CmsTutorialMapper, CmsTutorial> implements CmsTutorialService {
}
