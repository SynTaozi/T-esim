package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.I18nProduct;
import com.tesim.mapper.I18nProductMapper;
import com.tesim.service.I18nProductService;
import org.springframework.stereotype.Service;

@Service
public class I18nProductServiceImpl extends ServiceImpl<I18nProductMapper, I18nProduct> implements I18nProductService {
}
