package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.ProductDisplayRule;
import com.tesim.mapper.ProductDisplayRuleMapper;
import com.tesim.service.ProductDisplayRuleService;
import org.springframework.stereotype.Service;

@Service
public class ProductDisplayRuleServiceImpl extends ServiceImpl<ProductDisplayRuleMapper, ProductDisplayRule> implements ProductDisplayRuleService {
}
