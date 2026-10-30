package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.CmsFaq;
import com.tesim.mapper.CmsFaqMapper;
import com.tesim.service.CmsFaqService;
import org.springframework.stereotype.Service;

@Service
public class CmsFaqServiceImpl extends ServiceImpl<CmsFaqMapper, CmsFaq> implements CmsFaqService {
}
