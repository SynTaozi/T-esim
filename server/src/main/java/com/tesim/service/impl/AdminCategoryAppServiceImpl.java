package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.AdminCategoryAppService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tesim.common.BizException;
import com.tesim.dto.admin.AdminCategoryRequest;
import com.tesim.dto.admin.AdminCategoryVO;
import com.tesim.dto.admin.SortOrderItem;
import com.tesim.entity.I18nProductCategory;
import com.tesim.entity.ProductCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminCategoryAppServiceImpl implements AdminCategoryAppService {

    private final ProductCategoryService categoryService;
    private final I18nProductCategoryService i18nCategoryService;

    public List<AdminCategoryVO> listRoots() {
        return listByParent(null, null);
    }

    public List<AdminCategoryVO> listEnabledRoots() {
        return listByParent(null, 1);
    }

    public List<AdminCategoryVO> listSubs(Long categoryId) {
        if (categoryId != null) {
            return listByParent(categoryId, null);
        }
        return categoryService.list(new LambdaQueryWrapper<ProductCategory>()
                        .isNotNull(ProductCategory::getParentId)
                        .ne(ProductCategory::getParentId, 0)
                        .orderByAsc(ProductCategory::getSortOrder)
                        .orderByAsc(ProductCategory::getId))
                .stream().map(this::toVO).toList();
    }

    public List<AdminCategoryVO> listEnabledSubs(Long categoryId) {
        if (categoryId == null) {
            throw new BizException("请指定父分类");
        }
        return listByParent(categoryId, 1);
    }

    public List<AdminCategoryVO> tree() {
        List<AdminCategoryVO> roots = listRoots();
        return roots;
    }

    public AdminCategoryVO get(Long id) {
        ProductCategory c = require(id);
        return toVO(c);
    }

    @Transactional
    public AdminCategoryVO createRoot(AdminCategoryRequest req) {
        return create(req, null);
    }

    @Transactional
    public AdminCategoryVO createSub(AdminCategoryRequest req) {
        Long parentId = req.getCategoryId() != null ? req.getCategoryId() : req.getParentId();
        if (parentId == null || parentId <= 0) {
            throw new BizException("子分类必须指定父分类");
        }
        require(parentId);
        return create(req, parentId);
    }

    @Transactional
    public AdminCategoryVO update(Long id, AdminCategoryRequest req) {
        ProductCategory c = require(id);
        if (StringUtils.hasText(req.getCode())) {
            c.setCode(req.getCode().trim());
        }
        if (req.getIcon() != null) {
            c.setIcon(req.getIcon());
        }
        if (req.getSortOrder() != null) {
            c.setSortOrder(req.getSortOrder());
        }
        if (req.getStatus() != null) {
            c.setStatus(req.getStatus());
        }
        if (req.getShowOnFront() != null) {
            c.setShowOnFront(req.getShowOnFront());
        }
        if (req.getParentId() != null || req.getCategoryId() != null) {
            Long parent = req.getParentId() != null ? req.getParentId() : req.getCategoryId();
            if (parent != null && !Objects.equals(parent, id)) {
                c.setParentId(parent);
            }
        }
        categoryService.updateById(c);
        upsertI18n(id, "zh", req.getName(), req.getDescription());
        upsertI18n(id, "en", req.getNameEn(), null);
        return toVO(c);
    }

    @Transactional
    public void delete(Long id) {
        require(id);
        long children = categoryService.count(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getParentId, id));
        if (children > 0) {
            throw new BizException("请先删除子分类");
        }
        i18nCategoryService.remove(new LambdaQueryWrapper<I18nProductCategory>()
                .eq(I18nProductCategory::getCategoryId, id));
        categoryService.removeById(id);
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
            categoryService.update(new LambdaUpdateWrapper<ProductCategory>()
                    .eq(ProductCategory::getId, item.getId())
                    .set(ProductCategory::getSortOrder, item.getSortOrder() == null ? 0 : item.getSortOrder()));
        }
    }

    private AdminCategoryVO create(AdminCategoryRequest req, Long parentId) {
        if (!StringUtils.hasText(req.getName())) {
            throw new BizException("分类名称不能为空");
        }
        ProductCategory c = new ProductCategory();
        c.setParentId(parentId);
        if (StringUtils.hasText(req.getCode())) {
            c.setCode(req.getCode().trim());
        } else {
            String prefix = parentId != null && parentId > 0 ? "SUB" : "CAT";
            c.setCode(prefix + System.currentTimeMillis());
        }
        c.setIcon(req.getIcon());
        c.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        c.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        // 二级分类默认不进前台顶栏；大分类默认展示
        if (parentId != null && parentId > 0) {
            c.setShowOnFront(Boolean.TRUE.equals(req.getShowOnFront()));
        } else {
            c.setShowOnFront(req.getShowOnFront() == null || req.getShowOnFront());
        }
        categoryService.save(c);
        upsertI18n(c.getId(), "zh", req.getName(), req.getDescription());
        upsertI18n(c.getId(), "en", req.getNameEn(), null);
        return toVO(c);
    }

    private List<AdminCategoryVO> listByParent(Long parentId, Integer status) {
        LambdaQueryWrapper<ProductCategory> qw = new LambdaQueryWrapper<ProductCategory>()
                .orderByAsc(ProductCategory::getSortOrder)
                .orderByAsc(ProductCategory::getId);
        if (parentId == null || parentId == 0L) {
            qw.and(w -> w.isNull(ProductCategory::getParentId).or().eq(ProductCategory::getParentId, 0));
        } else {
            qw.eq(ProductCategory::getParentId, parentId);
        }
        if (status != null) {
            qw.eq(ProductCategory::getStatus, status);
        }
        return categoryService.list(qw).stream().map(this::toVO).toList();
    }

    private ProductCategory require(Long id) {
        ProductCategory c = categoryService.getById(id);
        if (c == null) {
            throw new BizException("分类不存在");
        }
        return c;
    }

    private void upsertI18n(Long categoryId, String locale, String name, String description) {
        if (!StringUtils.hasText(name) && description == null) {
            return;
        }
        I18nProductCategory existing = i18nCategoryService.getOne(new LambdaQueryWrapper<I18nProductCategory>()
                .eq(I18nProductCategory::getCategoryId, categoryId)
                .eq(I18nProductCategory::getLocale, locale));
        if (existing == null) {
            if (!StringUtils.hasText(name)) {
                return;
            }
            I18nProductCategory row = new I18nProductCategory();
            row.setCategoryId(categoryId);
            row.setLocale(locale);
            row.setName(name.trim());
            row.setDescription(description);
            i18nCategoryService.save(row);
        } else {
            if (StringUtils.hasText(name)) {
                existing.setName(name.trim());
            }
            if (description != null) {
                existing.setDescription(description);
            }
            i18nCategoryService.update(existing, new LambdaQueryWrapper<I18nProductCategory>()
                    .eq(I18nProductCategory::getCategoryId, categoryId)
                    .eq(I18nProductCategory::getLocale, locale));
        }
    }

    private AdminCategoryVO toVO(ProductCategory c) {
        I18nProductCategory zh = i18n(c.getId(), "zh");
        I18nProductCategory en = i18n(c.getId(), "en");
        Long parentId = c.getParentId();
        String parentName = null;
        if (parentId != null && parentId > 0) {
            I18nProductCategory pZh = i18n(parentId, "zh");
            parentName = pZh != null ? pZh.getName() : null;
        }
        String nameZh = zh != null ? zh.getName() : c.getCode();
        String descZh = zh != null ? zh.getDescription() : null;
        return AdminCategoryVO.builder()
                .id(c.getId())
                .parentId(parentId)
                .categoryId(parentId != null && parentId > 0 ? parentId : null)
                .categoryName(parentName)
                .code(c.getCode())
                .icon(c.getIcon())
                .name(nameZh)
                .nameZh(nameZh)
                .nameEn(en != null ? en.getName() : null)
                .description(descZh)
                .descriptionZh(descZh)
                .sortOrder(c.getSortOrder())
                .status(c.getStatus())
                .showOnFront(c.getShowOnFront())
                .build();
    }

    private I18nProductCategory i18n(Long categoryId, String locale) {
        return i18nCategoryService.getOne(new LambdaQueryWrapper<I18nProductCategory>()
                .eq(I18nProductCategory::getCategoryId, categoryId)
                .eq(I18nProductCategory::getLocale, locale));
    }
}
