package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.AdminProductAppService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.AdminProductRequest;
import com.tesim.dto.admin.AdminProductVO;
import com.tesim.dto.admin.AdminSkuRequest;
import com.tesim.dto.admin.AdminSkuVO;
import com.tesim.dto.admin.IdListRequest;
import com.tesim.dto.admin.SortOrderItem;
import com.tesim.entity.I18nProduct;
import com.tesim.entity.I18nProductCategory;
import com.tesim.entity.I18nProductSku;
import com.tesim.entity.Product;
import com.tesim.entity.ProductCoverage;
import com.tesim.entity.ProductSku;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminProductAppServiceImpl implements AdminProductAppService {

    private final ProductService productService;
    private final ProductSkuService productSkuService;
    private final ProductCoverageService coverageService;
    private final ProductCategoryService categoryService;
    private final I18nProductService i18nProductService;
    private final I18nProductSkuService i18nProductSkuService;
    private final I18nProductCategoryService i18nCategoryService;

    public PageResult<AdminProductVO> page(Integer page, Integer size, String name, Integer status,
                                           Integer itemType, Integer isPhysicalCard, Long subCategoryId,
                                           Long categoryId, String keyword) {
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size < 1 ? 10 : size;
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<Product>()
                .eq(Product::getDeleted, false)
                .orderByAsc(Product::getSortOrder)
                .orderByDesc(Product::getId);
        if (status != null) {
            qw.eq(Product::getStatus, status == 1 ? "ON_SALE" : "OFF_SALE");
        }
        if (isPhysicalCard != null) {
            qw.eq(Product::getIsPhysical, isPhysicalCard == 1);
        }
        Long cat = subCategoryId != null ? subCategoryId : categoryId;
        if (cat != null) {
            qw.eq(Product::getCategoryId, cat);
        }
        if (itemType != null) {
            qw.eq(Product::getProductKind, String.valueOf(itemType));
        }
        String kw = StringUtils.hasText(keyword) ? keyword : name;
        if (StringUtils.hasText(kw)) {
            List<Long> ids = i18nProductService.list(new LambdaQueryWrapper<I18nProduct>()
                            .like(I18nProduct::getName, kw.trim()))
                    .stream().map(I18nProduct::getProductId).distinct().toList();
            if (ids.isEmpty()) {
                qw.and(w -> w.like(Product::getSpuCode, kw.trim()).or().like(Product::getDataLimit, kw.trim()));
            } else {
                qw.and(w -> w.in(Product::getId, ids)
                        .or().like(Product::getSpuCode, kw.trim())
                        .or().like(Product::getDataLimit, kw.trim()));
            }
        }
        Page<Product> result = productService.page(new Page<>(p + 1L, s), qw);
        List<AdminProductVO> content = result.getRecords().stream().map(pr -> toVO(pr, false)).toList();
        return PageResult.of(content, result.getTotal(), p, s);
    }

    public AdminProductVO get(Long id) {
        return toVO(require(id), true);
    }

    @Transactional
    public AdminProductVO create(AdminProductRequest req) {
        if (!StringUtils.hasText(req.getName())) {
            throw new BizException("商品名称不能为空");
        }
        Product p = new Product();
        applyProduct(p, req, true);
        p.setDeleted(false);
        if (!StringUtils.hasText(p.getSpuCode())) {
            p.setSpuCode("SPU" + System.currentTimeMillis());
        }
        productService.save(p);
        upsertI18n(p.getId(), req);
        replaceCoverage(p.getId(), req);
        ensureDefaultSku(p, req);
        if (req.getSkus() != null && !req.getSkus().isEmpty()) {
            for (AdminSkuRequest skuReq : req.getSkus()) {
                createSkuInternal(p.getId(), skuReq);
            }
        }
        return toVO(p, true);
    }

    @Transactional
    public AdminProductVO update(Long id, AdminProductRequest req) {
        Product p = require(id);
        applyProduct(p, req, false);
        productService.updateById(p);
        // 布尔标记显式落库，避免 updateById 策略漏更
        if (req.getIsFeatured() != null || req.getIsHot() != null || req.getIsPhysical() != null
                || req.getIsPhysicalCard() != null) {
            LambdaUpdateWrapper<Product> uw = new LambdaUpdateWrapper<Product>().eq(Product::getId, id);
            if (req.getIsFeatured() != null) {
                uw.set(Product::getIsFeatured, req.getIsFeatured());
            }
            if (req.getIsHot() != null) {
                uw.set(Product::getIsHot, req.getIsHot());
            }
            if (req.getIsPhysical() != null) {
                uw.set(Product::getIsPhysical, req.getIsPhysical());
            } else if (req.getIsPhysicalCard() != null) {
                uw.set(Product::getIsPhysical, req.getIsPhysicalCard() == 1);
            }
            productService.update(uw);
        }
        upsertI18n(id, req);
        if (req.getApplicableRegions() != null || req.getCountries() != null) {
            replaceCoverage(id, req);
        }
        if (req.getSkus() != null) {
            syncSkus(id, req.getSkus());
        } else if (req.getPrice() != null || req.getOriginalPrice() != null || req.getStock() != null) {
            syncDefaultSku(p, req);
        }
        return toVO(require(id), true);
    }

    @Transactional
    public void delete(Long id) {
        Product p = require(id);
        p.setDeleted(true);
        p.setStatus("OFF_SALE");
        productService.updateById(p);
    }

    @Transactional
    public void batchDelete(IdListRequest req) {
        if (req == null || req.getIds() == null) {
            return;
        }
        for (Long id : req.getIds()) {
            delete(id);
        }
    }

    @Transactional
    public AdminProductVO copy(Long id) {
        Product src = require(id);
        AdminProductVO vo = toVO(src, true);
        AdminProductRequest req = new AdminProductRequest();
        req.setName((vo.getName() == null ? "Copy" : vo.getName()) + " (副本)");
        req.setNameEn(vo.getNameEn());
        req.setDescription(vo.getDescription());
        req.setDescriptionEn(vo.getDescriptionEn());
        req.setFeatures(vo.getFeatures());
        req.setFeaturesEn(vo.getFeaturesEn());
        req.setPrice(vo.getPrice());
        req.setOriginalPrice(vo.getOriginalPrice());
        req.setPhysicalCost(vo.getPhysicalCost());
        req.setCostPrice(vo.getCostPrice());
        req.setApplicableRegions(vo.getApplicableRegions());
        req.setDataLimit(vo.getDataLimit());
        req.setValidityDays(vo.getValidityDays());
        req.setStock(vo.getStock());
        req.setImageUrl(vo.getImageUrl());
        req.setDisplayImageUrl(vo.getDisplayImageUrl());
        req.setStatus(0);
        req.setSubCategoryId(vo.getSubCategoryId());
        req.setCategoryId(vo.getCategoryId());
        req.setPlanType(vo.getPlanType());
        req.setSmsSupport(vo.getSmsSupport());
        req.setDataSupport(vo.getDataSupport());
        req.setVoiceSupport(vo.getVoiceSupport());
        req.setTopupType(vo.getTopupType());
        req.setIsPhysicalCard(vo.getIsPhysicalCard());
        req.setSortOrder(vo.getSortOrder());
        req.setProductKind(vo.getProductKind());
        req.setSpuCode(null);
        req.setIsFeatured(false);
        req.setIsHot(false);
        if (vo.getSkus() != null) {
            List<AdminSkuRequest> skus = new ArrayList<>();
            for (AdminSkuVO s : vo.getSkus()) {
                AdminSkuRequest sr = new AdminSkuRequest();
                sr.setSkuCode(null);
                sr.setLabel(s.getLabel());
                sr.setLabelEn(s.getLabelEn());
                sr.setMonths(s.getMonths());
                sr.setDurationDays(s.getDurationDays());
                sr.setSalePrice(s.getSalePrice());
                sr.setPrice(s.getPrice());
                sr.setPricePerMonth(s.getPricePerMonth());
                sr.setOriginalPrice(s.getOriginalPrice());
                sr.setStock(s.getStock());
                sr.setIsDefault(s.getIsDefault());
                sr.setStatus(s.getStatus());
                sr.setSortOrder(s.getSortOrder());
                skus.add(sr);
            }
            req.setSkus(skus);
        }
        return create(req);
    }

    @Transactional
    public void patchStatus(Long id, Integer status) {
        Product p = require(id);
        p.setStatus(status != null && status == 1 ? "ON_SALE" : "OFF_SALE");
        productService.updateById(p);
    }

    @Transactional
    public void batchStatus(IdListRequest req) {
        if (req == null || req.getIds() == null) {
            return;
        }
        String st = req.getStatus() != null && req.getStatus() == 1 ? "ON_SALE" : "OFF_SALE";
        productService.update(new LambdaUpdateWrapper<Product>()
                .in(Product::getId, req.getIds())
                .eq(Product::getDeleted, false)
                .set(Product::getStatus, st));
    }

    @Transactional
    public void batchSort(List<SortOrderItem> items) {
        if (items == null) {
            return;
        }
        for (SortOrderItem item : items) {
            if (item.getId() == null) {
                continue;
            }
            productService.update(new LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, item.getId())
                    .set(Product::getSortOrder, item.getSortOrder() == null ? 0 : item.getSortOrder()));
        }
    }

    @Transactional
    public void batchUpdate(List<AdminProductRequest> items) {
        // minimal: ignore if empty; FE may send id via nested field — not used yet
        if (items == null) {
            return;
        }
    }

    public List<AdminSkuVO> listSkus(Long productId) {
        require(productId);
        return productSkuService.list(new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, productId)
                        .orderByAsc(ProductSku::getSortOrder)
                        .orderByAsc(ProductSku::getId))
                .stream().map(this::toSkuVO).toList();
    }

    public AdminSkuVO getSku(Long productId, Long skuId) {
        return toSkuVO(requireSku(productId, skuId));
    }

    @Transactional
    public AdminSkuVO createSku(Long productId, AdminSkuRequest req) {
        require(productId);
        return toSkuVO(createSkuInternal(productId, req));
    }

    @Transactional
    public AdminSkuVO updateSku(Long productId, Long skuId, AdminSkuRequest req) {
        ProductSku sku = requireSku(productId, skuId);
        applySku(sku, req, false);
        productSkuService.updateById(sku);
        upsertSkuI18n(sku.getId(), req);
        return toSkuVO(sku);
    }

    @Transactional
    public void deleteSku(Long productId, Long skuId) {
        requireSku(productId, skuId);
        i18nProductSkuService.remove(new LambdaQueryWrapper<I18nProductSku>()
                .eq(I18nProductSku::getSkuId, skuId));
        productSkuService.removeById(skuId);
    }

    @Transactional
    public List<AdminSkuVO> batchSaveSkus(Long productId, List<AdminSkuRequest> reqs) {
        require(productId);
        if (reqs == null) {
            return listSkus(productId);
        }
        syncSkus(productId, reqs);
        return listSkus(productId);
    }

    /**
     * 同步定价档：按 id 更新/新建，删除未提交的旧档。允许同一产品下 months 重复。
     */
    private void syncSkus(Long productId, List<AdminSkuRequest> reqs) {
        List<ProductSku> existing = productSkuService.list(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getProductId, productId));
        Set<Long> incomingIds = new HashSet<>();
        for (AdminSkuRequest req : reqs) {
            if (req != null && req.getId() != null) {
                incomingIds.add(req.getId());
            }
        }
        // 先删掉移除的档，再更新/新建
        for (ProductSku old : existing) {
            if (!incomingIds.contains(old.getId())) {
                i18nProductSkuService.remove(new LambdaQueryWrapper<I18nProductSku>()
                        .eq(I18nProductSku::getSkuId, old.getId()));
                productSkuService.removeById(old.getId());
            }
        }
        int idx = 0;
        for (AdminSkuRequest req : reqs) {
            if (req == null) {
                continue;
            }
            if (req.getSortOrder() == null) {
                req.setSortOrder(idx);
            }
            idx++;
            if (req.getId() != null) {
                ProductSku sku = productSkuService.getById(req.getId());
                if (sku == null || !Objects.equals(sku.getProductId(), productId)) {
                    throw new BizException("定价档不存在: " + req.getId());
                }
                applySku(sku, req, false);
                productSkuService.updateById(sku);
                upsertSkuI18n(sku.getId(), req);
            } else {
                createSkuInternal(productId, req);
            }
        }
        // 保证至少有一个默认档
        List<ProductSku> after = productSkuService.list(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getProductId, productId)
                .orderByAsc(ProductSku::getSortOrder)
                .orderByAsc(ProductSku::getId));
        if (!after.isEmpty() && after.stream().noneMatch(s -> Boolean.TRUE.equals(s.getIsDefault()))) {
            ProductSku first = after.get(0);
            first.setIsDefault(true);
            productSkuService.updateById(first);
        }
    }

    private ProductSku createSkuInternal(Long productId, AdminSkuRequest req) {
        ProductSku sku = new ProductSku();
        sku.setProductId(productId);
        applySku(sku, req, true);
        if (!StringUtils.hasText(sku.getSkuCode())) {
            sku.setSkuCode("SKU" + productId + "-" + System.currentTimeMillis());
        }
        productSkuService.save(sku);
        upsertSkuI18n(sku.getId(), req);
        return sku;
    }

    private void applySku(ProductSku sku, AdminSkuRequest req, boolean creating) {
        if (req == null) {
            if (creating) {
                sku.setSalePrice(BigDecimal.ZERO);
                sku.setStatus(1);
                sku.setSortOrder(0);
                sku.setStock(0);
                sku.setIsDefault(false);
            }
            return;
        }
        if (StringUtils.hasText(req.getSkuCode())) {
            sku.setSkuCode(req.getSkuCode().trim());
        }
        if (req.getMonths() != null) {
            sku.setMonths(req.getMonths());
        }
        if (req.getDurationDays() != null) {
            sku.setDurationDays(req.getDurationDays());
        }
        BigDecimal price = req.getSalePrice() != null ? req.getSalePrice() : req.getPrice();
        if (price != null) {
            sku.setSalePrice(price);
        } else if (creating) {
            sku.setSalePrice(BigDecimal.ZERO);
        }
        if (req.getPricePerMonth() != null) {
            sku.setPricePerMonth(req.getPricePerMonth());
        }
        if (req.getOriginalPrice() != null) {
            sku.setOriginalPrice(req.getOriginalPrice());
        }
        if (req.getStock() != null) {
            sku.setStock(req.getStock());
        } else if (creating) {
            sku.setStock(0);
        }
        if (req.getIsDefault() != null) {
            sku.setIsDefault(req.getIsDefault());
        } else if (creating) {
            sku.setIsDefault(false);
        }
        if (req.getStatus() != null) {
            sku.setStatus(req.getStatus());
        } else if (creating) {
            sku.setStatus(1);
        }
        if (req.getSortOrder() != null) {
            sku.setSortOrder(req.getSortOrder());
        } else if (creating) {
            sku.setSortOrder(0);
        }
    }

    private void applyProduct(Product p, AdminProductRequest req, boolean creating) {
        Long cat = req.getSubCategoryId() != null ? req.getSubCategoryId() : req.getCategoryId();
        if (cat != null) {
            p.setCategoryId(cat);
        }
        if (StringUtils.hasText(req.getSpuCode())) {
            p.setSpuCode(req.getSpuCode().trim());
        }
        if (StringUtils.hasText(req.getProductKind())) {
            p.setProductKind(req.getProductKind());
        } else if (req.getItemType() != null) {
            p.setProductKind(String.valueOf(req.getItemType()));
        } else if (creating) {
            p.setProductKind("ESIM");
        }
        if (req.getPlanType() != null) {
            p.setPlanMode(req.getPlanType());
        }
        if (req.getTopupType() != null) {
            p.setTopupMode(req.getTopupType());
        }
        if (req.getDataLimit() != null) {
            p.setDataLimit(req.getDataLimit());
        }
        if (req.getValidityDays() != null) {
            p.setBaseValidityDays(req.getValidityDays());
        } else if (req.getBaseValidityDays() != null) {
            p.setBaseValidityDays(req.getBaseValidityDays());
        }
        if (req.getSmsSupport() != null) {
            p.setSupportSms(req.getSmsSupport() == 1);
        }
        if (req.getDataSupport() != null) {
            p.setSupportData(req.getDataSupport() == 1);
        } else if (creating) {
            p.setSupportData(true);
        }
        if (req.getVoiceSupport() != null) {
            p.setSupportVoice(req.getVoiceSupport() == 1);
        }
        if (req.getIsPhysicalCard() != null) {
            p.setIsPhysical(req.getIsPhysicalCard() == 1);
        } else if (req.getIsPhysical() != null) {
            p.setIsPhysical(req.getIsPhysical());
        } else if (creating) {
            p.setIsPhysical(false);
        }
        if (req.getCostPrice() != null) {
            p.setCostPrice(req.getCostPrice());
        }
        if (req.getPhysicalCost() != null) {
            p.setPhysicalCost(req.getPhysicalCost());
        }
        if (req.getImageUrl() != null) {
            p.setImageUrl(req.getImageUrl());
        }
        if (req.getDisplayImageUrl() != null) {
            p.setDisplayImageUrl(req.getDisplayImageUrl());
        }
        if (req.getStatus() != null) {
            p.setStatus(req.getStatus() == 1 ? "ON_SALE" : "OFF_SALE");
        } else if (creating) {
            p.setStatus("OFF_SALE");
        }
        if (req.getIsFeatured() != null) {
            p.setIsFeatured(req.getIsFeatured());
        } else if (creating) {
            p.setIsFeatured(false);
        }
        if (req.getIsHot() != null) {
            p.setIsHot(req.getIsHot());
        } else if (creating) {
            p.setIsHot(false);
        }
        if (req.getSortOrder() != null) {
            p.setSortOrder(req.getSortOrder());
        } else if (creating) {
            p.setSortOrder(0);
        }
    }

    private void ensureDefaultSku(Product p, AdminProductRequest req) {
        if (req.getSkus() != null && !req.getSkus().isEmpty()) {
            return;
        }
        AdminSkuRequest skuReq = new AdminSkuRequest();
        skuReq.setSalePrice(req.getPrice() == null ? BigDecimal.ZERO : req.getPrice());
        skuReq.setOriginalPrice(req.getOriginalPrice());
        skuReq.setStock(req.getStock() == null ? 0 : req.getStock());
        skuReq.setDurationDays(req.getValidityDays());
        skuReq.setIsDefault(true);
        skuReq.setStatus(1);
        skuReq.setSortOrder(0);
        skuReq.setLabel(req.getName());
        skuReq.setLabelEn(req.getNameEn());
        createSkuInternal(p.getId(), skuReq);
    }

    private void syncDefaultSku(Product p, AdminProductRequest req) {
        ProductSku sku = productSkuService.getOne(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getProductId, p.getId())
                .eq(ProductSku::getIsDefault, true)
                .last("LIMIT 1"));
        if (sku == null) {
            sku = productSkuService.getOne(new LambdaQueryWrapper<ProductSku>()
                    .eq(ProductSku::getProductId, p.getId())
                    .orderByAsc(ProductSku::getSortOrder)
                    .last("LIMIT 1"));
        }
        if (sku == null) {
            ensureDefaultSku(p, req);
            return;
        }
        if (req.getPrice() != null) {
            sku.setSalePrice(req.getPrice());
        }
        if (req.getOriginalPrice() != null) {
            sku.setOriginalPrice(req.getOriginalPrice());
        }
        if (req.getStock() != null) {
            sku.setStock(req.getStock());
        }
        if (req.getValidityDays() != null) {
            sku.setDurationDays(req.getValidityDays());
        }
        productSkuService.updateById(sku);
    }

    private void replaceCoverage(Long productId, AdminProductRequest req) {
        coverageService.remove(new LambdaQueryWrapper<ProductCoverage>()
                .eq(ProductCoverage::getProductId, productId));
        List<String> regions = req.getApplicableRegions();
        if ((regions == null || regions.isEmpty()) && StringUtils.hasText(req.getCountries())) {
            regions = Arrays.stream(req.getCountries().split("[,，\\s]+"))
                    .map(String::trim).filter(StringUtils::hasText).toList();
        }
        if (regions == null) {
            return;
        }
        for (String code : regions) {
            ProductCoverage cov = new ProductCoverage();
            cov.setProductId(productId);
            cov.setCountryCode(code.toUpperCase());
            cov.setRegionCode(code.toUpperCase());
            coverageService.save(cov);
        }
    }

    private void upsertI18n(Long productId, AdminProductRequest req) {
        String nameZh = StringUtils.hasText(req.getName()) ? req.getName() : req.getNameZh();
        upsertProductI18n(productId, "zh", nameZh, req.getSummaryZh(), req.getDescription(),
                req.getFeatures(), req.getNotesZh());
        upsertProductI18n(productId, "en", req.getNameEn(), req.getSummaryEn(), req.getDescriptionEn(),
                req.getFeaturesEn(), req.getNotesEn());
    }

    private void upsertProductI18n(Long productId, String locale, String name, String summary,
                                   String description, String features, String notes) {
        if (!StringUtils.hasText(name) && summary == null && description == null
                && features == null && notes == null) {
            return;
        }
        I18nProduct existing = i18nProductService.getOne(new LambdaQueryWrapper<I18nProduct>()
                .eq(I18nProduct::getProductId, productId)
                .eq(I18nProduct::getLocale, locale));
        if (existing == null) {
            if (!StringUtils.hasText(name)) {
                return;
            }
            I18nProduct row = new I18nProduct();
            row.setProductId(productId);
            row.setLocale(locale);
            row.setName(name.trim());
            row.setSummary(summary);
            row.setDescription(description);
            row.setFeatures(features);
            row.setNotes(notes);
            i18nProductService.save(row);
        } else {
            LambdaUpdateWrapper<I18nProduct> uw = new LambdaUpdateWrapper<I18nProduct>()
                    .eq(I18nProduct::getProductId, productId)
                    .eq(I18nProduct::getLocale, locale);
            if (StringUtils.hasText(name)) {
                uw.set(I18nProduct::getName, name.trim());
            }
            if (summary != null) {
                uw.set(I18nProduct::getSummary, summary);
            }
            if (description != null) {
                uw.set(I18nProduct::getDescription, description);
            }
            if (features != null) {
                uw.set(I18nProduct::getFeatures, features);
            }
            if (notes != null) {
                uw.set(I18nProduct::getNotes, notes);
            }
            i18nProductService.update(uw);
        }
    }

    private void upsertSkuI18n(Long skuId, AdminSkuRequest req) {
        if (req == null) {
            return;
        }
        upsertSkuLabel(skuId, "zh", req.getLabel());
        upsertSkuLabel(skuId, "en", req.getLabelEn());
    }

    private void upsertSkuLabel(Long skuId, String locale, String label) {
        if (!StringUtils.hasText(label)) {
            return;
        }
        I18nProductSku existing = i18nProductSkuService.getOne(new LambdaQueryWrapper<I18nProductSku>()
                .eq(I18nProductSku::getSkuId, skuId)
                .eq(I18nProductSku::getLocale, locale));
        if (existing == null) {
            I18nProductSku row = new I18nProductSku();
            row.setSkuId(skuId);
            row.setLocale(locale);
            row.setLabel(label.trim());
            i18nProductSkuService.save(row);
        } else {
            existing.setLabel(label.trim());
            i18nProductSkuService.update(existing, new LambdaQueryWrapper<I18nProductSku>()
                    .eq(I18nProductSku::getSkuId, skuId)
                    .eq(I18nProductSku::getLocale, locale));
        }
    }

    private AdminProductVO toVO(Product p, boolean withSkus) {
        I18nProduct zh = i18nProduct(p.getId(), "zh");
        I18nProduct en = i18nProduct(p.getId(), "en");
        List<ProductSku> skus = productSkuService.list(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getProductId, p.getId())
                .orderByAsc(ProductSku::getSortOrder));
        ProductSku def = skus.stream().filter(s -> Boolean.TRUE.equals(s.getIsDefault())).findFirst()
                .orElse(skus.isEmpty() ? null : skus.get(0));
        List<ProductCoverage> coverages = coverageService.list(new LambdaQueryWrapper<ProductCoverage>()
                .eq(ProductCoverage::getProductId, p.getId()));
        List<String> regions = coverages.stream()
                .map(c -> StringUtils.hasText(c.getCountryCode()) ? c.getCountryCode() : c.getRegionCode())
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        String catName = null;
        if (p.getCategoryId() != null) {
            I18nProductCategory catZh = i18nCategoryService.getOne(new LambdaQueryWrapper<I18nProductCategory>()
                    .eq(I18nProductCategory::getCategoryId, p.getCategoryId())
                    .eq(I18nProductCategory::getLocale, "zh"));
            catName = catZh != null ? catZh.getName() : null;
        }
        List<AdminSkuVO> skuVos = withSkus ? skus.stream().map(this::toSkuVO).toList() : null;
        String nameZh = zh != null ? zh.getName() : p.getSpuCode();
        return AdminProductVO.builder()
                .id(p.getId())
                .name(nameZh)
                .nameZh(nameZh)
                .nameEn(en != null ? en.getName() : null)
                .description(zh != null ? zh.getDescription() : null)
                .descriptionEn(en != null ? en.getDescription() : null)
                .summaryZh(zh != null ? zh.getSummary() : null)
                .summaryEn(en != null ? en.getSummary() : null)
                .features(zh != null ? zh.getFeatures() : null)
                .featuresEn(en != null ? en.getFeatures() : null)
                .notesZh(zh != null ? zh.getNotes() : null)
                .notesEn(en != null ? en.getNotes() : null)
                .price(def != null ? def.getSalePrice() : BigDecimal.ZERO)
                .originalPrice(def != null ? def.getOriginalPrice() : null)
                .physicalCost(p.getPhysicalCost())
                .costPrice(p.getCostPrice())
                .applicableRegions(regions)
                .countries(String.join(",", regions))
                .dataLimit(p.getDataLimit())
                .validityDays(p.getBaseValidityDays())
                .stock(def != null ? def.getStock() : 0)
                .imageUrl(p.getImageUrl())
                .displayImageUrl(p.getDisplayImageUrl())
                .status("ON_SALE".equals(p.getStatus()) ? 1 : 0)
                .productType(p.getPlanMode())
                .itemType(parseIntSafe(p.getProductKind()))
                .categoryId(p.getCategoryId())
                .categoryName(catName)
                .subCategoryId(p.getCategoryId())
                .planType(p.getPlanMode())
                .smsSupport(Boolean.TRUE.equals(p.getSupportSms()) ? 1 : 0)
                .dataSupport(Boolean.TRUE.equals(p.getSupportData()) ? 1 : 0)
                .voiceSupport(Boolean.TRUE.equals(p.getSupportVoice()) ? 1 : 0)
                .topupType(p.getTopupMode())
                .isPhysicalCard(Boolean.TRUE.equals(p.getIsPhysical()) ? 1 : 0)
                .sortOrder(p.getSortOrder())
                .spuCode(p.getSpuCode())
                .productKind(p.getProductKind())
                .isFeatured(p.getIsFeatured())
                .isHot(p.getIsHot())
                .skus(skuVos)
                .pricingTiers(skuVos)
                .build();
    }

    private AdminSkuVO toSkuVO(ProductSku sku) {
        I18nProductSku zh = i18nProductSkuService.getOne(new LambdaQueryWrapper<I18nProductSku>()
                .eq(I18nProductSku::getSkuId, sku.getId())
                .eq(I18nProductSku::getLocale, "zh"));
        I18nProductSku en = i18nProductSkuService.getOne(new LambdaQueryWrapper<I18nProductSku>()
                .eq(I18nProductSku::getSkuId, sku.getId())
                .eq(I18nProductSku::getLocale, "en"));
        return AdminSkuVO.builder()
                .id(sku.getId())
                .productId(sku.getProductId())
                .skuCode(sku.getSkuCode())
                .label(zh != null ? zh.getLabel() : sku.getSkuCode())
                .labelEn(en != null ? en.getLabel() : null)
                .months(sku.getMonths())
                .durationDays(sku.getDurationDays())
                .salePrice(sku.getSalePrice())
                .price(sku.getSalePrice())
                .pricePerMonth(sku.getPricePerMonth())
                .originalPrice(sku.getOriginalPrice())
                .stock(sku.getStock())
                .isDefault(sku.getIsDefault())
                .status(sku.getStatus())
                .sortOrder(sku.getSortOrder())
                .build();
    }

    private I18nProduct i18nProduct(Long productId, String locale) {
        return i18nProductService.getOne(new LambdaQueryWrapper<I18nProduct>()
                .eq(I18nProduct::getProductId, productId)
                .eq(I18nProduct::getLocale, locale));
    }

    private Product require(Long id) {
        Product p = productService.getById(id);
        if (p == null || Boolean.TRUE.equals(p.getDeleted())) {
            throw new BizException("商品不存在");
        }
        return p;
    }

    private ProductSku requireSku(Long productId, Long skuId) {
        ProductSku sku = productSkuService.getById(skuId);
        if (sku == null || !Objects.equals(sku.getProductId(), productId)) {
            throw new BizException("SKU 不存在");
        }
        return sku;
    }

    private static Integer parseIntSafe(String v) {
        if (!StringUtils.hasText(v)) {
            return null;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
