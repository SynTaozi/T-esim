package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.ProductCoverage;
import com.tesim.mapper.ProductCoverageMapper;
import com.tesim.service.ProductCoverageService;
import org.springframework.stereotype.Service;

@Service
public class ProductCoverageServiceImpl extends ServiceImpl<ProductCoverageMapper, ProductCoverage> implements ProductCoverageService {
}
