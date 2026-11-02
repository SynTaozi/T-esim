package com.tesim.service;

import com.tesim.common.BizException;
import com.tesim.dto.product.CategoryVO;
import com.tesim.dto.product.PricingTierVO;
import com.tesim.dto.product.ProductListVO;
import com.tesim.dto.product.ProductVO;
import com.tesim.dto.product.RegionCountsVO;
import com.tesim.entity.*;
import com.tesim.util.LocaleHelper;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;

public interface ProductCatalogService {

    ProductListVO listProducts(String region, Integer page, Integer size, Long itemType, Long subCategoryId);



    ProductListVO search(String keyword, Integer page, Integer size, Long itemType, Long subCategoryId);



    List<ProductVO> featured(Integer size);



    ProductVO detail(Long id);



    List<PricingTierVO> pricingTiers(Long productId);



    List<String> regions();



    RegionCountsVO regionCounts();



    List<CategoryVO> categories();



    List<CategoryVO> subcategories(Long categoryId);


}
