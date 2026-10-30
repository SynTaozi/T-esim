package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.CmsFaqCategory;
import com.tesim.mapper.CmsFaqCategoryMapper;
import com.tesim.service.CmsFaqCategoryService;
import org.springframework.stereotype.Service;

@Service
public class CmsFaqCategoryServiceImpl extends ServiceImpl<CmsFaqCategoryMapper, CmsFaqCategory> implements CmsFaqCategoryService {
}
