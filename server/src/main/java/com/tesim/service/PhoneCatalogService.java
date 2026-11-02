package com.tesim.service;

import com.tesim.common.BizException;
import com.tesim.dto.phone.PhoneCatalogPageVO;
import com.tesim.dto.phone.PhoneDeviceVO;
import com.tesim.dto.phone.PhoneNumberVO;
import com.tesim.entity.I18nProduct;
import com.tesim.entity.Product;
import com.tesim.entity.ProductCoverage;
import com.tesim.entity.ProductSku;
import com.tesim.util.LocaleHelper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Map;

public interface PhoneCatalogService {

    PhoneCatalogPageVO catalog(String region, Integer page, Integer size);




    PhoneNumberVO detail(Long id);




    List<String> regions();




    List<PhoneDeviceVO> devices();


}
