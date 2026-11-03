package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.ProductCatalogService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tesim.common.BizException;
import com.tesim.dto.product.CategoryVO;
import com.tesim.dto.product.PricingTierVO;
import com.tesim.dto.product.ProductListVO;
import com.tesim.dto.product.ProductVO;
import com.tesim.dto.product.RegionCountsVO;
import com.tesim.entity.*;
import com.tesim.util.LocaleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductCatalogServiceImpl implements ProductCatalogService {

    private final ProductService productService;
    private final ProductSkuService productSkuService;
    private final ProductCoverageService productCoverageService;
    private final ProductCategoryService productCategoryService;
    private final I18nProductService i18nProductService;
    private final I18nProductCategoryService i18nProductCategoryService;
    private final I18nProductSkuService i18nProductSkuService;

    public ProductListVO listProducts(String region, Integer page, Integer size, Long itemType, Long subCategoryId) {
        int p = page == null ? 0 : page;
        int s = size == null ? 12 : size;
        LambdaQueryWrapper<Product> qw = baseOnSale();
        applyCategoryFilter(qw, itemType, subCategoryId);
        qw.orderByAsc(Product::getSortOrder).orderByDesc(Product::getId);
        Page<Product> result = productService.page(new Page<>(p + 1L, s), qw);
        List<ProductVO> products = result.getRecords().stream()
                .map(this::toFrontProduct)
                .filter(vo -> matchRegion(vo, region))
                .collect(Collectors.toList());
        return ProductListVO.builder()
                .products(products)
                .total(result.getTotal())
                .page(p)
                .size(s)
                .build();
    }

    public ProductListVO search(String keyword, Integer page, Integer size, Long itemType, Long subCategoryId) {
        int p = page == null ? 0 : page;
        int s = size == null ? 12 : size;
        String locale = LocaleHelper.current();
        Set<Long> ids = new HashSet<>();
        if (StringUtils.hasText(keyword)) {
            List<I18nProduct> i18n = i18nProductService.list(new LambdaQueryWrapper<I18nProduct>()
                    .eq(I18nProduct::getLocale, locale)
                    .like(I18nProduct::getName, keyword.trim()));
            i18n.forEach(x -> ids.add(x.getProductId()));
        }
        LambdaQueryWrapper<Product> qw = baseOnSale();
        if (!ids.isEmpty()) {
            qw.in(Product::getId, ids);
        } else if (StringUtils.hasText(keyword)) {
            qw.like(Product::getSpuCode, keyword.trim());
        }
        applyCategoryFilter(qw, itemType, subCategoryId);
        qw.orderByAsc(Product::getSortOrder).orderByDesc(Product::getId);
        Page<Product> result = productService.page(new Page<>(p + 1L, s), qw);
        return ProductListVO.builder()
                .products(result.getRecords().stream().map(this::toFrontProduct).toList())
                .total(result.getTotal())
                .page(p)
                .size(s)
                .build();
    }

    /** 二级分类优先；否则按大类及其子类过滤（商品挂在二级分类上） */
    private void applyCategoryFilter(LambdaQueryWrapper<Product> qw, Long rootId, Long subCategoryId) {
        if (subCategoryId != null && subCategoryId > 0) {
            qw.eq(Product::getCategoryId, subCategoryId);
            return;
        }
        if (rootId == null || rootId <= 0) {
            return;
        }
        List<Long> childIds = productCategoryService.list(new LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getParentId, rootId)
                        .eq(ProductCategory::getStatus, 1))
                .stream()
                .map(ProductCategory::getId)
                .toList();
        if (childIds.isEmpty()) {
            qw.eq(Product::getCategoryId, rootId);
        } else {
            qw.and(w -> w.eq(Product::getCategoryId, rootId).or().in(Product::getCategoryId, childIds));
        }
    }

    public List<ProductVO> featured(Integer size) {
        int s = size == null ? 8 : size;
        List<Product> list = productService.list(baseOnSale()
                .eq(Product::getIsFeatured, true)
                .orderByAsc(Product::getSortOrder)
                .orderByDesc(Product::getId)
                .last("LIMIT " + s));
        return list.stream().map(this::toFrontProduct).toList();
    }

    public ProductVO detail(Long id) {
        Product p = productService.getById(id);
        if (p == null || Boolean.TRUE.equals(p.getDeleted())) {
            throw new BizException("商品不存在");
        }
        return toFrontProduct(p);
    }

    public List<PricingTierVO> pricingTiers(Long productId) {
        List<ProductSku> skus = productSkuService.list(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getProductId, productId)
                .eq(ProductSku::getStatus, 1)
                .orderByAsc(ProductSku::getSortOrder)
                .orderByAsc(ProductSku::getMonths));
        String locale = LocaleHelper.current();
        return skus.stream().map(sku -> {
            I18nProductSku i18n = i18nProductSkuService.getOne(new LambdaQueryWrapper<I18nProductSku>()
                    .eq(I18nProductSku::getSkuId, sku.getId())
                    .eq(I18nProductSku::getLocale, locale));
            BigDecimal sale = sku.getSalePrice() == null ? BigDecimal.ZERO : sku.getSalePrice();
            BigDecimal perMonth = sku.getPricePerMonth();
            if (perMonth == null && sku.getMonths() != null && sku.getMonths() > 0) {
                perMonth = sale.divide(BigDecimal.valueOf(sku.getMonths()), 2, java.math.RoundingMode.HALF_UP);
            }
            return PricingTierVO.builder()
                    .id(sku.getId())
                    .productId(sku.getProductId())
                    .months(sku.getMonths())
                    .salePrice(sale)
                    .totalPrice(sale)
                    .price(sale)
                    .pricePerMonth(perMonth)
                    .originalPrice(sku.getOriginalPrice())
                    .durationDays(sku.getDurationDays())
                    .label(i18n != null ? i18n.getLabel() : null)
                    .isDefault(sku.getIsDefault())
                    .sortOrder(sku.getSortOrder())
                    .isActive(1)
                    .build();
        }).toList();
    }

    public List<String> regions() {
        return productCoverageService.list().stream()
                .map(ProductCoverage::getRegionCode)
                .filter(StringUtils::hasText)
                .distinct()
                .sorted()
                .toList();
    }

    public RegionCountsVO regionCounts() {
        Map<String, Long> map = new HashMap<>();
        for (ProductCoverage c : productCoverageService.list()) {
            if (StringUtils.hasText(c.getRegionCode())) {
                map.merge(c.getRegionCode(), 1L, Long::sum);
            }
        }
        return new RegionCountsVO(map);
    }

    public List<CategoryVO> categories() {
        // 前台顶栏只展示大分类；二级分类走侧边悬浮栏
        return listCategoryVos(null, true);
    }

    public List<CategoryVO> subcategories(Long categoryId) {
        if (categoryId == null) {
            return List.of();
        }
        return listCategoryVos(categoryId, false);
    }

    private List<CategoryVO> listCategoryVos(Long parentId, boolean rootsOnlyFront) {
        String locale = LocaleHelper.current();
        LambdaQueryWrapper<ProductCategory> qw = new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getStatus, 1)
                .orderByAsc(ProductCategory::getSortOrder)
                .orderByAsc(ProductCategory::getId);
        if (parentId == null) {
            qw.and(w -> w.isNull(ProductCategory::getParentId).or().eq(ProductCategory::getParentId, 0));
            if (rootsOnlyFront) {
                qw.eq(ProductCategory::getShowOnFront, true);
            }
        } else {
            qw.eq(ProductCategory::getParentId, parentId);
        }
        return productCategoryService.list(qw).stream().map(c -> {
            I18nProductCategory zh = i18nProductCategoryService.getOne(new LambdaQueryWrapper<I18nProductCategory>()
                    .eq(I18nProductCategory::getCategoryId, c.getId())
                    .eq(I18nProductCategory::getLocale, "zh"));
            I18nProductCategory en = i18nProductCategoryService.getOne(new LambdaQueryWrapper<I18nProductCategory>()
                    .eq(I18nProductCategory::getCategoryId, c.getId())
                    .eq(I18nProductCategory::getLocale, "en"));
            I18nProductCategory cur = "en".equals(locale) ? (en != null ? en : zh) : (zh != null ? zh : en);
            String nameZh = zh != null ? zh.getName() : c.getCode();
            String nameEn = en != null ? en.getName() : null;
            return CategoryVO.builder()
                    .id(c.getId())
                    .code(c.getCode())
                    .parentId(c.getParentId())
                    .name(cur != null ? cur.getName() : c.getCode())
                    .nameZh(nameZh)
                    .nameEn(nameEn)
                    .description(cur != null ? cur.getDescription() : null)
                    .icon(c.getIcon())
                    .sortOrder(c.getSortOrder())
                    .build();
        }).toList();
    }

    private LambdaQueryWrapper<Product> baseOnSale() {
        return new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, "ON_SALE")
                .eq(Product::getDeleted, false);
    }

    private boolean matchRegion(ProductVO product, String region) {
        if (!StringUtils.hasText(region) || "all".equalsIgnoreCase(region)) {
            return true;
        }
        String regions = product.getApplicableRegions();
        return regions != null && regions.toLowerCase().contains(region.toLowerCase());
    }

    private ProductVO toFrontProduct(Product p) {
        String locale = LocaleHelper.current();
        I18nProduct zh = i18nProductService.getOne(new LambdaQueryWrapper<I18nProduct>()
                .eq(I18nProduct::getProductId, p.getId()).eq(I18nProduct::getLocale, "zh"));
        I18nProduct en = i18nProductService.getOne(new LambdaQueryWrapper<I18nProduct>()
                .eq(I18nProduct::getProductId, p.getId()).eq(I18nProduct::getLocale, "en"));
        I18nProduct cur = "en".equals(locale) ? (en != null ? en : zh) : (zh != null ? zh : en);

        List<ProductCoverage> coverages = productCoverageService.list(new LambdaQueryWrapper<ProductCoverage>()
                .eq(ProductCoverage::getProductId, p.getId()));
        List<String> countries = coverages.stream().map(ProductCoverage::getCountryCode).toList();
        List<String> regions = coverages.stream().map(ProductCoverage::getRegionCode)
                .filter(StringUtils::hasText).distinct().toList();

        ProductSku defaultSku = productSkuService.getOne(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getProductId, p.getId())
                .eq(ProductSku::getStatus, 1)
                .orderByDesc(ProductSku::getIsDefault)
                .orderByAsc(ProductSku::getSortOrder)
                .last("LIMIT 1"));
        BigDecimal price = defaultSku != null ? defaultSku.getSalePrice() : BigDecimal.ZERO;

        ProductCategory cat = p.getCategoryId() != null ? productCategoryService.getById(p.getCategoryId()) : null;
        I18nProductCategory catI18n = null;
        if (cat != null) {
            catI18n = i18nProductCategoryService.getOne(new LambdaQueryWrapper<I18nProductCategory>()
                    .eq(I18nProductCategory::getCategoryId, cat.getId())
                    .eq(I18nProductCategory::getLocale, locale));
        }

        return ProductVO.builder()
                .id(p.getId())
                .name(cur != null ? cur.getName() : p.getSpuCode())
                .nameEn(en != null ? en.getName() : null)
                .description(zh != null ? zh.getDescription() : null)
                .descriptionEn(en != null ? en.getDescription() : null)
                .summary(zh != null ? zh.getSummary() : null)
                .summaryEn(en != null ? en.getSummary() : null)
                .features(zh != null ? zh.getFeatures() : null)
                .featuresEn(en != null ? en.getFeatures() : null)
                .notes(zh != null ? zh.getNotes() : null)
                .notesEn(en != null ? en.getNotes() : null)
                .importantLabels(zh != null ? zh.getNotes() : null)
                .importantLabelsEn(en != null ? en.getNotes() : null)
                .price(price)
                .dataLimit(p.getDataLimit())
                .validityDays(p.getBaseValidityDays())
                .stock(defaultSku != null ? defaultSku.getStock() : null)
                .imageUrl(p.getImageUrl())
                .displayImageUrl(p.getDisplayImageUrl())
                .status("ON_SALE".equals(p.getStatus()) ? 1 : 0)
                .planType(p.getPlanMode())
                .topupType(p.getTopupMode())
                .smsSupport(Boolean.TRUE.equals(p.getSupportSms()) ? 1 : 0)
                .dataSupport(Boolean.TRUE.equals(p.getSupportData()) ? 1 : 0)
                .voiceSupport(Boolean.TRUE.equals(p.getSupportVoice()) ? 1 : 0)
                .isPhysicalCard(Boolean.TRUE.equals(p.getIsPhysical()) ? 1 : 0)
                .isFeatured(Boolean.TRUE.equals(p.getIsFeatured()))
                .isHot(Boolean.TRUE.equals(p.getIsHot()))
                .itemType(p.getCategoryId())
                .categoryName(catI18n != null ? catI18n.getName() : (cat != null ? cat.getCode() : null))
                .categoryCode(cat != null ? cat.getCode() : null)
                .countries(toJsonArray(countries))
                .applicableRegions(toJsonArray(regions))
                .sortOrder(p.getSortOrder())
                .productKind(p.getProductKind())
                .spuCode(p.getSpuCode())
                .build();
    }

    private String toJsonArray(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        return list.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",", "[", "]"));
    }
}
