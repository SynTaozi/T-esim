package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.PhoneCatalogService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tesim.common.BizException;
import com.tesim.dto.phone.PhoneCatalogPageVO;
import com.tesim.dto.phone.PhoneDeviceVO;
import com.tesim.dto.phone.PhoneNumberVO;
import com.tesim.entity.I18nProduct;
import com.tesim.entity.Product;
import com.tesim.entity.ProductCoverage;
import com.tesim.entity.ProductSku;
import com.tesim.util.LocaleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhoneCatalogServiceImpl implements PhoneCatalogService {

    private final ProductService productService;
    private final ProductSkuService productSkuService;
    private final ProductCoverageService productCoverageService;
    private final I18nProductService i18nProductService;

    public PhoneCatalogPageVO catalog(String region, Integer page, Integer size) {
        int p = page == null ? 0 : page;
        int s = size == null ? 12 : size;
        Page<Product> result = productService.page(new Page<>(p + 1L, s), phonePlansQuery());
        List<PhoneNumberVO> numbers = result.getRecords().stream()
                .map(this::toPhoneNumber)
                .filter(vo -> matchRegion(vo, region))
                .toList();
        int totalPages = s <= 0 ? 0 : (int) ((result.getTotal() + s - 1) / s);
        return PhoneCatalogPageVO.builder()
                .numbers(numbers)
                .total(result.getTotal())
                .totalPages(totalPages)
                .currentPage(p)
                .build();
    }

    public PhoneNumberVO detail(Long id) {
        Product p = productService.getById(id);
        if (p == null || Boolean.TRUE.equals(p.getDeleted()) || !"ON_SALE".equals(p.getStatus())) {
            throw new BizException(404, "号码套餐不存在");
        }
        if (!isPhonePlan(p)) {
            throw new BizException(404, "号码套餐不存在");
        }
        return toPhoneNumber(p);
    }

    public List<String> regions() {
        return productCoverageService.list().stream()
                .map(ProductCoverage::getRegionCode)
                .filter(StringUtils::hasText)
                .distinct()
                .sorted()
                .toList();
    }

    public List<PhoneDeviceVO> devices() {
        List<Product> list = productService.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getDeleted, false)
                .eq(Product::getStatus, "ON_SALE")
                .eq(Product::getProductKind, "DEVICE")
                .orderByAsc(Product::getSortOrder)
                .orderByDesc(Product::getId));
        return list.stream().map(this::toDevice).toList();
    }

    private LambdaQueryWrapper<Product> phonePlansQuery() {
        return new LambdaQueryWrapper<Product>()
                .eq(Product::getDeleted, false)
                .eq(Product::getStatus, "ON_SALE")
                .and(w -> w.eq(Product::getSupportVoice, true)
                        .or().eq(Product::getSupportSms, true)
                        .or().eq(Product::getProductKind, "PLAN"))
                .orderByAsc(Product::getSortOrder)
                .orderByDesc(Product::getId);
    }

    private boolean isPhonePlan(Product p) {
        return Boolean.TRUE.equals(p.getSupportVoice())
                || Boolean.TRUE.equals(p.getSupportSms())
                || "PLAN".equalsIgnoreCase(p.getProductKind());
    }

    private PhoneNumberVO toPhoneNumber(Product p) {
        String locale = LocaleHelper.current();
        I18nProduct zh = i18n(p.getId(), "zh");
        I18nProduct en = i18n(p.getId(), "en");
        I18nProduct cur = "en".equals(locale) ? (en != null ? en : zh) : (zh != null ? zh : en);
        ProductSku sku = defaultSku(p.getId());
        List<ProductCoverage> coverages = productCoverageService.list(new LambdaQueryWrapper<ProductCoverage>()
                .eq(ProductCoverage::getProductId, p.getId()));
        String country = coverages.stream().map(ProductCoverage::getCountryCode).filter(StringUtils::hasText).findFirst().orElse("");
        String region = coverages.stream().map(ProductCoverage::getRegionCode).filter(StringUtils::hasText).findFirst().orElse("");
        List<String> features = parseFeatures(cur != null ? cur.getFeatures() : null);

        return PhoneNumberVO.builder()
                .id(p.getId())
                .name(cur != null ? cur.getName() : p.getSpuCode())
                .nameEn(en != null ? en.getName() : null)
                .countryCode(country)
                .country(country)
                .countryEn(country)
                .region(region)
                .operator(p.getSupplierFlag())
                .features(features)
                .description(cur != null ? cur.getDescription() : null)
                .numberType(Boolean.TRUE.equals(p.getIsPhysical()) ? "PHYSICAL" : "ESIM")
                .tips(List.of())
                .warnings(List.of())
                .monthlyPrice(sku != null ? sku.getSalePrice() : BigDecimal.ZERO)
                .maintenancePrice(sku != null ? sku.getPricePerMonth() : BigDecimal.ZERO)
                .originalPrice(sku != null ? sku.getOriginalPrice() : null)
                .stock(sku != null ? sku.getStock() : null)
                .whatsappTip(null)
                .status(1)
                .build();
    }

    private PhoneDeviceVO toDevice(Product p) {
        String locale = LocaleHelper.current();
        I18nProduct zh = i18n(p.getId(), "zh");
        I18nProduct en = i18n(p.getId(), "en");
        I18nProduct cur = "en".equals(locale) ? (en != null ? en : zh) : (zh != null ? zh : en);
        ProductSku sku = defaultSku(p.getId());
        BigDecimal price = sku != null && sku.getSalePrice() != null ? sku.getSalePrice() : BigDecimal.ZERO;
        boolean free = price.compareTo(BigDecimal.ZERO) <= 0;
        return PhoneDeviceVO.builder()
                .id(p.getId())
                .name(cur != null ? cur.getName() : p.getSpuCode())
                .nameEn(en != null ? en.getName() : null)
                .description(cur != null ? cur.getDescription() : null)
                .descriptionEn(en != null ? en.getDescription() : null)
                .price(price)
                .isFree(free)
                .deviceType(p.getProductKind())
                .shippingFee(BigDecimal.ZERO)
                .build();
    }

    private ProductSku defaultSku(Long productId) {
        return productSkuService.getOne(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getProductId, productId)
                .eq(ProductSku::getStatus, 1)
                .orderByDesc(ProductSku::getIsDefault)
                .orderByAsc(ProductSku::getSortOrder)
                .last("LIMIT 1"));
    }

    private I18nProduct i18n(Long productId, String locale) {
        return i18nProductService.getOne(new LambdaQueryWrapper<I18nProduct>()
                .eq(I18nProduct::getProductId, productId)
                .eq(I18nProduct::getLocale, locale));
    }

    private boolean matchRegion(PhoneNumberVO vo, String region) {
        if (!StringUtils.hasText(region) || "all".equalsIgnoreCase(region)) {
            return true;
        }
        return (vo.getRegion() != null && vo.getRegion().equalsIgnoreCase(region))
                || (vo.getCountryCode() != null && vo.getCountryCode().equalsIgnoreCase(region));
    }

    private List<String> parseFeatures(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        String t = raw.trim();
        if (t.startsWith("[") && t.endsWith("]")) {
            t = t.substring(1, t.length() - 1);
            return Arrays.stream(t.split(","))
                    .map(s -> s.trim().replaceAll("^\"|\"$", ""))
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
        }
        return Arrays.stream(t.split("[,;|/]"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }
}
