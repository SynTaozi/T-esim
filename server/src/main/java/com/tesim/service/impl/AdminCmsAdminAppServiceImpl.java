package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.AdminCmsAdminAppService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.*;
import com.tesim.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminCmsAdminAppServiceImpl implements AdminCmsAdminAppService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CmsFaqCategoryService faqCategoryService;
    private final CmsFaqService faqService;
    private final I18nCmsFaqCategoryService i18nFaqCategoryService;
    private final I18nCmsFaqService i18nFaqService;
    private final CmsTutorialCategoryService tutorialCategoryService;
    private final CmsTutorialService tutorialService;
    private final I18nCmsTutorialCategoryService i18nTutorialCategoryService;
    private final I18nCmsTutorialService i18nTutorialService;
    private final CmsAppDownloadService appDownloadService;
    private final I18nCmsAppDownloadService i18nAppDownloadService;
    private final PaymentChannelService paymentChannelService;
    private final I18nPaymentChannelService i18nPaymentChannelService;
    private final CouponService couponService;
    private final CouponUsageService couponUsageService;
    private final CmsSiteSettingService siteSettingService;
    private final SysThemeService themeService;

    // ==================== FAQ ====================

    public List<AdminFaqCategoryVO> listFaqCategories() {
        return faqCategoryService.list(new LambdaQueryWrapper<CmsFaqCategory>()
                        .orderByAsc(CmsFaqCategory::getSortOrder)
                        .orderByAsc(CmsFaqCategory::getId))
                .stream().map(this::toFaqCategoryVO).toList();
    }

    @Transactional
    public AdminFaqCategoryVO createFaqCategory(AdminFaqCategoryRequest req) {
        if (!StringUtils.hasText(req.getName())) {
            throw new BizException("分类名称不能为空");
        }
        CmsFaqCategory c = new CmsFaqCategory();
        c.setSortOrder(sortOf(req.getSortOrder(), req.getSort()));
        c.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        faqCategoryService.save(c);
        upsertFaqCategoryI18n(c.getId(), "zh", req.getName());
        upsertFaqCategoryI18n(c.getId(), "en", req.getNameEn());
        return toFaqCategoryVO(c);
    }

    @Transactional
    public AdminFaqCategoryVO updateFaqCategory(Long id, AdminFaqCategoryRequest req) {
        CmsFaqCategory c = requireFaqCategory(id);
        if (req.getSortOrder() != null || req.getSort() != null) {
            c.setSortOrder(sortOf(req.getSortOrder(), req.getSort()));
        }
        if (req.getStatus() != null) {
            c.setStatus(req.getStatus());
        }
        faqCategoryService.updateById(c);
        upsertFaqCategoryI18n(id, "zh", req.getName());
        upsertFaqCategoryI18n(id, "en", req.getNameEn());
        return toFaqCategoryVO(c);
    }

    @Transactional
    public void deleteFaqCategory(Long id) {
        requireFaqCategory(id);
        long count = faqService.count(new LambdaQueryWrapper<CmsFaq>().eq(CmsFaq::getCategoryId, id));
        if (count > 0) {
            throw new BizException("请先删除该分类下的 FAQ");
        }
        i18nFaqCategoryService.remove(new LambdaQueryWrapper<I18nCmsFaqCategory>()
                .eq(I18nCmsFaqCategory::getCategoryId, id));
        faqCategoryService.removeById(id);
    }

    public List<AdminFaqVO> listFaqs(Long categoryId) {
        LambdaQueryWrapper<CmsFaq> qw = new LambdaQueryWrapper<CmsFaq>()
                .orderByAsc(CmsFaq::getSortOrder)
                .orderByAsc(CmsFaq::getId);
        if (categoryId != null) {
            qw.eq(CmsFaq::getCategoryId, categoryId);
        }
        return faqService.list(qw).stream().map(this::toFaqVO).toList();
    }

    @Transactional
    public AdminFaqVO createFaq(AdminFaqRequest req) {
        if (req.getCategoryId() == null) {
            throw new BizException("请选择分类");
        }
        if (!StringUtils.hasText(req.getQuestion())) {
            throw new BizException("问题不能为空");
        }
        requireFaqCategory(req.getCategoryId());
        CmsFaq f = new CmsFaq();
        f.setCategoryId(req.getCategoryId());
        f.setSortOrder(sortOf(req.getSortOrder(), req.getSort()));
        f.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        faqService.save(f);
        upsertFaqI18n(f.getId(), "zh", req.getQuestion(), req.getAnswer());
        upsertFaqI18n(f.getId(), "en", req.getQuestionEn(), req.getAnswerEn());
        return toFaqVO(f);
    }

    @Transactional
    public AdminFaqVO updateFaq(Long id, AdminFaqRequest req) {
        CmsFaq f = requireFaq(id);
        if (req.getCategoryId() != null) {
            requireFaqCategory(req.getCategoryId());
            f.setCategoryId(req.getCategoryId());
        }
        if (req.getSortOrder() != null || req.getSort() != null) {
            f.setSortOrder(sortOf(req.getSortOrder(), req.getSort()));
        }
        if (req.getStatus() != null) {
            f.setStatus(req.getStatus());
        }
        faqService.updateById(f);
        upsertFaqI18n(id, "zh", req.getQuestion(), req.getAnswer());
        upsertFaqI18n(id, "en", req.getQuestionEn(), req.getAnswerEn());
        return toFaqVO(f);
    }

    @Transactional
    public void deleteFaq(Long id) {
        requireFaq(id);
        i18nFaqService.remove(new LambdaQueryWrapper<I18nCmsFaq>().eq(I18nCmsFaq::getFaqId, id));
        faqService.removeById(id);
    }

    // ==================== Tutorial ====================

    public List<AdminTutorialCategoryVO> listTutorialCategories() {
        return tutorialCategoryService.list(new LambdaQueryWrapper<CmsTutorialCategory>()
                        .orderByAsc(CmsTutorialCategory::getSortOrder)
                        .orderByAsc(CmsTutorialCategory::getId))
                .stream().map(this::toTutorialCategoryVO).toList();
    }

    @Transactional
    public AdminTutorialCategoryVO createTutorialCategory(AdminTutorialCategoryRequest req) {
        if (!StringUtils.hasText(req.getName())) {
            throw new BizException("分类名称不能为空");
        }
        CmsTutorialCategory c = new CmsTutorialCategory();
        c.setIcon(req.getIcon());
        c.setSortOrder(sortOf(req.getSortOrder(), req.getSort()));
        c.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        tutorialCategoryService.save(c);
        upsertTutorialCategoryI18n(c.getId(), "zh", req.getName());
        upsertTutorialCategoryI18n(c.getId(), "en", req.getNameEn());
        return toTutorialCategoryVO(c);
    }

    @Transactional
    public AdminTutorialCategoryVO updateTutorialCategory(Long id, AdminTutorialCategoryRequest req) {
        CmsTutorialCategory c = requireTutorialCategory(id);
        if (req.getIcon() != null) {
            c.setIcon(req.getIcon());
        }
        if (req.getSortOrder() != null || req.getSort() != null) {
            c.setSortOrder(sortOf(req.getSortOrder(), req.getSort()));
        }
        if (req.getStatus() != null) {
            c.setStatus(req.getStatus());
        }
        tutorialCategoryService.updateById(c);
        upsertTutorialCategoryI18n(id, "zh", req.getName());
        upsertTutorialCategoryI18n(id, "en", req.getNameEn());
        return toTutorialCategoryVO(c);
    }

    @Transactional
    public void deleteTutorialCategory(Long id) {
        requireTutorialCategory(id);
        long count = tutorialService.count(new LambdaQueryWrapper<CmsTutorial>().eq(CmsTutorial::getCategoryId, id));
        if (count > 0) {
            throw new BizException("请先删除该分类下的教程");
        }
        i18nTutorialCategoryService.remove(new LambdaQueryWrapper<I18nCmsTutorialCategory>()
                .eq(I18nCmsTutorialCategory::getCategoryId, id));
        tutorialCategoryService.removeById(id);
    }

    public List<AdminTutorialVO> listTutorials(Long categoryId, String contentType) {
        LambdaQueryWrapper<CmsTutorial> qw = new LambdaQueryWrapper<CmsTutorial>()
                .orderByAsc(CmsTutorial::getSortOrder)
                .orderByAsc(CmsTutorial::getId);
        if (categoryId != null) {
            qw.eq(CmsTutorial::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(contentType)) {
            qw.eq(CmsTutorial::getContentType, contentType.trim());
        }
        return tutorialService.list(qw).stream().map(this::toTutorialVO).toList();
    }

    @Transactional
    public AdminTutorialVO createTutorial(AdminTutorialRequest req) {
        if (req.getCategoryId() == null) {
            throw new BizException("请选择分类");
        }
        if (!StringUtils.hasText(req.getTitle())) {
            throw new BizException("标题不能为空");
        }
        requireTutorialCategory(req.getCategoryId());
        CmsTutorial t = new CmsTutorial();
        applyTutorial(t, req);
        tutorialService.save(t);
        upsertTutorialI18n(t.getId(), "zh", req.getTitle(), req.getSummary(), req.getContent());
        upsertTutorialI18n(t.getId(), "en", req.getTitleEn(), req.getSummaryEn(), req.getContentEn());
        return toTutorialVO(t);
    }

    @Transactional
    public AdminTutorialVO updateTutorial(Long id, AdminTutorialRequest req) {
        CmsTutorial t = requireTutorial(id);
        applyTutorial(t, req);
        tutorialService.updateById(t);
        upsertTutorialI18n(id, "zh", req.getTitle(), req.getSummary(), req.getContent());
        upsertTutorialI18n(id, "en", req.getTitleEn(), req.getSummaryEn(), req.getContentEn());
        return toTutorialVO(t);
    }

    @Transactional
    public void deleteTutorial(Long id) {
        requireTutorial(id);
        i18nTutorialService.remove(new LambdaQueryWrapper<I18nCmsTutorial>().eq(I18nCmsTutorial::getTutorialId, id));
        tutorialService.removeById(id);
    }

    // ==================== App Download ====================

    public List<AdminAppDownloadVO> listAppDownloads() {
        return appDownloadService.list(new LambdaQueryWrapper<CmsAppDownload>()
                        .orderByAsc(CmsAppDownload::getSortOrder)
                        .orderByAsc(CmsAppDownload::getId))
                .stream().map(this::toAppDownloadVO).toList();
    }

    public AdminAppDownloadVO getAppDownload(Long id) {
        return toAppDownloadVO(requireAppDownload(id));
    }

    @Transactional
    public AdminAppDownloadVO createAppDownload(AdminAppDownloadRequest req) {
        if (!StringUtils.hasText(req.getPlatform()) || !StringUtils.hasText(req.getDownloadUrl())) {
            throw new BizException("平台和下载地址不能为空");
        }
        CmsAppDownload d = new CmsAppDownload();
        applyAppDownload(d, req);
        appDownloadService.save(d);
        upsertAppDownloadI18n(d.getId(), "zh", req.getName(), req.getRemark());
        upsertAppDownloadI18n(d.getId(), "en", req.getNameEn(), null);
        return toAppDownloadVO(d);
    }

    @Transactional
    public AdminAppDownloadVO updateAppDownload(Long id, AdminAppDownloadRequest req) {
        CmsAppDownload d = requireAppDownload(id);
        applyAppDownload(d, req);
        appDownloadService.updateById(d);
        upsertAppDownloadI18n(id, "zh", req.getName(), req.getRemark());
        upsertAppDownloadI18n(id, "en", req.getNameEn(), null);
        return toAppDownloadVO(d);
    }

    @Transactional
    public void deleteAppDownload(Long id) {
        requireAppDownload(id);
        i18nAppDownloadService.remove(new LambdaQueryWrapper<I18nCmsAppDownload>()
                .eq(I18nCmsAppDownload::getDownloadId, id));
        appDownloadService.removeById(id);
    }

    // ==================== Payment Methods ====================

    public List<AdminPaymentMethodVO> listPaymentMethods(Boolean enabledOnly) {
        LambdaQueryWrapper<PaymentChannel> qw = new LambdaQueryWrapper<PaymentChannel>()
                .orderByAsc(PaymentChannel::getSortOrder)
                .orderByAsc(PaymentChannel::getId);
        if (Boolean.TRUE.equals(enabledOnly)) {
            qw.eq(PaymentChannel::getEnabled, true);
        }
        return paymentChannelService.list(qw).stream().map(this::toPaymentVO).toList();
    }

    @Transactional
    public AdminPaymentMethodVO savePaymentMethod(AdminPaymentMethodRequest req) {
        PaymentChannel c;
        if (req.getId() != null) {
            c = requirePayment(req.getId());
            if (StringUtils.hasText(req.getCode())) {
                c.setCode(req.getCode().trim());
            }
        } else {
            if (!StringUtils.hasText(req.getCode())) {
                throw new BizException("支付方式编码不能为空");
            }
            c = new PaymentChannel();
            c.setCode(req.getCode().trim());
        }
        if (req.getIcon() != null) {
            c.setIcon(req.getIcon());
        }
        if (req.getEnabled() != null) {
            c.setEnabled(req.getEnabled());
        } else if (c.getEnabled() == null) {
            c.setEnabled(true);
        }
        if (req.getSortOrder() != null) {
            c.setSortOrder(req.getSortOrder());
        } else if (c.getSortOrder() == null) {
            c.setSortOrder(0);
        }
        if (req.getExtraJson() != null) {
            c.setExtraJson(req.getExtraJson());
        }
        if (c.getId() == null) {
            paymentChannelService.save(c);
        } else {
            paymentChannelService.updateById(c);
        }
        String nameZh = StringUtils.hasText(req.getName()) ? req.getName() : req.getNameZh();
        upsertPaymentI18n(c.getId(), "zh", nameZh, req.getDescription());
        upsertPaymentI18n(c.getId(), "en", req.getNameEn(), req.getDescriptionEn());
        return toPaymentVO(c);
    }

    @Transactional
    public void updatePaymentEnabled(Long id, Boolean enabled) {
        PaymentChannel c = requirePayment(id);
        c.setEnabled(Boolean.TRUE.equals(enabled));
        paymentChannelService.updateById(c);
    }

    @Transactional
    public void deletePaymentMethod(Long id) {
        requirePayment(id);
        i18nPaymentChannelService.remove(new LambdaQueryWrapper<I18nPaymentChannel>()
                .eq(I18nPaymentChannel::getChannelId, id));
        paymentChannelService.removeById(id);
    }

    // ==================== Coupons ====================

    public List<AdminCouponVO> listCoupons() {
        return couponService.list(new LambdaQueryWrapper<Coupon>().orderByDesc(Coupon::getId))
                .stream().map(this::toCouponVO).toList();
    }

    @Transactional
    public AdminCouponVO createCoupon(AdminCouponRequest req) {
        if (!StringUtils.hasText(req.getCode()) || req.getDiscountAmount() == null) {
            throw new BizException("优惠码和金额不能为空");
        }
        Coupon c = new Coupon();
        applyCoupon(c, req);
        if (!StringUtils.hasText(c.getStatus())) {
            c.setStatus("ACTIVE");
        }
        couponService.save(c);
        return toCouponVO(c);
    }

    @Transactional
    public AdminCouponVO updateCoupon(Long id, AdminCouponRequest req) {
        Coupon c = requireCoupon(id);
        applyCoupon(c, req);
        couponService.updateById(c);
        return toCouponVO(c);
    }

    @Transactional
    public AdminCouponVO disableCoupon(Long id) {
        Coupon c = requireCoupon(id);
        c.setStatus("DISABLED");
        couponService.updateById(c);
        return toCouponVO(c);
    }

    public PageResult<AdminCouponUsageVO> pageCouponUsages(Long couponId, Integer page, Integer size) {
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size < 1 ? 20 : size;
        LambdaQueryWrapper<CouponUsage> qw = new LambdaQueryWrapper<CouponUsage>()
                .orderByDesc(CouponUsage::getId);
        if (couponId != null) {
            qw.eq(CouponUsage::getCouponId, couponId);
        }
        Page<CouponUsage> result = couponUsageService.page(new Page<>(p + 1L, s), qw);
        List<AdminCouponUsageVO> content = result.getRecords().stream().map(u -> AdminCouponUsageVO.builder()
                .id(u.getId())
                .couponId(u.getCouponId())
                .couponCode(u.getCouponCode())
                .orderId(u.getOrderId())
                .orderNo(u.getOrderNo())
                .memberId(u.getMemberId())
                .discountAmount(u.getDiscountAmount())
                .status(u.getStatus())
                .usedAt(u.getUsedAt() == null ? null : u.getUsedAt().format(FMT))
                .build()).toList();
        return PageResult.of(content, result.getTotal(), p, s);
    }

    // ==================== Site Display ====================

    public AdminSiteDisplayRequest getSiteDisplay() {
        CmsSiteSetting s = siteSettingService.getOne(new LambdaQueryWrapper<CmsSiteSetting>()
                .eq(CmsSiteSetting::getCode, "default").last("LIMIT 1"));
        AdminSiteDisplayRequest vo = new AdminSiteDisplayRequest();
        if (s == null) {
            vo.setDefaultShippingFee(BigDecimal.ZERO);
            return vo;
        }
        vo.setLoginBannerUrl(s.getLoginBannerUrl());
        vo.setRegisterBannerUrl(s.getRegisterBannerUrl());
        vo.setHomeHeroImageUrl(s.getHomeHeroImageUrl());
        vo.setDefaultShippingFee(s.getDefaultShippingFee());
        return vo;
    }

    @Transactional
    public AdminSiteDisplayRequest updateSiteDisplay(AdminSiteDisplayRequest req) {
        CmsSiteSetting s = siteSettingService.getOne(new LambdaQueryWrapper<CmsSiteSetting>()
                .eq(CmsSiteSetting::getCode, "default").last("LIMIT 1"));
        if (s == null) {
            s = new CmsSiteSetting();
            s.setCode("default");
        }
        if (req.getLoginBannerUrl() != null) {
            s.setLoginBannerUrl(req.getLoginBannerUrl());
        }
        if (req.getRegisterBannerUrl() != null) {
            s.setRegisterBannerUrl(req.getRegisterBannerUrl());
        }
        if (req.getHomeHeroImageUrl() != null) {
            s.setHomeHeroImageUrl(req.getHomeHeroImageUrl());
        }
        if (req.getDefaultShippingFee() != null) {
            s.setDefaultShippingFee(req.getDefaultShippingFee());
        }
        if (s.getId() == null) {
            siteSettingService.save(s);
        } else {
            siteSettingService.updateById(s);
        }
        return getSiteDisplay();
    }

    // ==================== Theme ====================

    public Map<String, String> getTheme() {
        SysTheme theme = themeService.getOne(new LambdaQueryWrapper<SysTheme>().last("LIMIT 1"));
        Map<String, String> map = new HashMap<>();
        if (theme == null) {
            map.put("themeId", "apple");
            map.put("themeName", "Apple");
            return map;
        }
        map.put("themeId", theme.getThemeKey());
        map.put("themeName", theme.getThemeName());
        map.put("code", theme.getCode());
        return map;
    }

    @Transactional
    public Map<String, String> setTheme(AdminThemeRequest req) {
        if (req == null || !StringUtils.hasText(req.getThemeId())) {
            throw new BizException("themeId 不能为空");
        }
        SysTheme theme = themeService.getOne(new LambdaQueryWrapper<SysTheme>().last("LIMIT 1"));
        if (theme == null) {
            theme = new SysTheme();
            theme.setCode("default");
        }
        theme.setThemeKey(req.getThemeId().trim());
        theme.setThemeName(StringUtils.hasText(req.getThemeName()) ? req.getThemeName().trim() : req.getThemeId().trim());
        if (theme.getId() == null) {
            themeService.save(theme);
        } else {
            themeService.updateById(theme);
        }
        return getTheme();
    }

    // ==================== helpers ====================

    private static int sortOf(Integer sortOrder, Integer sort) {
        if (sortOrder != null) {
            return sortOrder;
        }
        return sort == null ? 0 : sort;
    }

    private CmsFaqCategory requireFaqCategory(Long id) {
        CmsFaqCategory c = faqCategoryService.getById(id);
        if (c == null) {
            throw new BizException("FAQ 分类不存在");
        }
        return c;
    }

    private CmsFaq requireFaq(Long id) {
        CmsFaq f = faqService.getById(id);
        if (f == null) {
            throw new BizException("FAQ 不存在");
        }
        return f;
    }

    private CmsTutorialCategory requireTutorialCategory(Long id) {
        CmsTutorialCategory c = tutorialCategoryService.getById(id);
        if (c == null) {
            throw new BizException("教程分类不存在");
        }
        return c;
    }

    private CmsTutorial requireTutorial(Long id) {
        CmsTutorial t = tutorialService.getById(id);
        if (t == null) {
            throw new BizException("教程不存在");
        }
        return t;
    }

    private CmsAppDownload requireAppDownload(Long id) {
        CmsAppDownload d = appDownloadService.getById(id);
        if (d == null) {
            throw new BizException("下载配置不存在");
        }
        return d;
    }

    private PaymentChannel requirePayment(Long id) {
        PaymentChannel c = paymentChannelService.getById(id);
        if (c == null) {
            throw new BizException("支付方式不存在");
        }
        return c;
    }

    private Coupon requireCoupon(Long id) {
        Coupon c = couponService.getById(id);
        if (c == null) {
            throw new BizException("优惠券不存在");
        }
        return c;
    }

    private void upsertFaqCategoryI18n(Long categoryId, String locale, String name) {
        if (!StringUtils.hasText(name)) {
            return;
        }
        I18nCmsFaqCategory existing = i18nFaqCategoryService.getOne(new LambdaQueryWrapper<I18nCmsFaqCategory>()
                .eq(I18nCmsFaqCategory::getCategoryId, categoryId)
                .eq(I18nCmsFaqCategory::getLocale, locale));
        if (existing == null) {
            I18nCmsFaqCategory row = new I18nCmsFaqCategory();
            row.setCategoryId(categoryId);
            row.setLocale(locale);
            row.setName(name.trim());
            i18nFaqCategoryService.save(row);
        } else {
            existing.setName(name.trim());
            i18nFaqCategoryService.update(existing, new LambdaQueryWrapper<I18nCmsFaqCategory>()
                    .eq(I18nCmsFaqCategory::getCategoryId, categoryId)
                    .eq(I18nCmsFaqCategory::getLocale, locale));
        }
    }

    private void upsertFaqI18n(Long faqId, String locale, String question, String answer) {
        if (!StringUtils.hasText(question) && answer == null) {
            return;
        }
        I18nCmsFaq existing = i18nFaqService.getOne(new LambdaQueryWrapper<I18nCmsFaq>()
                .eq(I18nCmsFaq::getFaqId, faqId)
                .eq(I18nCmsFaq::getLocale, locale));
        if (existing == null) {
            if (!StringUtils.hasText(question)) {
                return;
            }
            I18nCmsFaq row = new I18nCmsFaq();
            row.setFaqId(faqId);
            row.setLocale(locale);
            row.setQuestion(question.trim());
            row.setAnswer(answer);
            i18nFaqService.save(row);
        } else {
            if (StringUtils.hasText(question)) {
                existing.setQuestion(question.trim());
            }
            if (answer != null) {
                existing.setAnswer(answer);
            }
            i18nFaqService.update(existing, new LambdaQueryWrapper<I18nCmsFaq>()
                    .eq(I18nCmsFaq::getFaqId, faqId)
                    .eq(I18nCmsFaq::getLocale, locale));
        }
    }

    private void upsertTutorialCategoryI18n(Long categoryId, String locale, String name) {
        if (!StringUtils.hasText(name)) {
            return;
        }
        I18nCmsTutorialCategory existing = i18nTutorialCategoryService.getOne(new LambdaQueryWrapper<I18nCmsTutorialCategory>()
                .eq(I18nCmsTutorialCategory::getCategoryId, categoryId)
                .eq(I18nCmsTutorialCategory::getLocale, locale));
        if (existing == null) {
            I18nCmsTutorialCategory row = new I18nCmsTutorialCategory();
            row.setCategoryId(categoryId);
            row.setLocale(locale);
            row.setName(name.trim());
            i18nTutorialCategoryService.save(row);
        } else {
            existing.setName(name.trim());
            i18nTutorialCategoryService.update(existing, new LambdaQueryWrapper<I18nCmsTutorialCategory>()
                    .eq(I18nCmsTutorialCategory::getCategoryId, categoryId)
                    .eq(I18nCmsTutorialCategory::getLocale, locale));
        }
    }

    private void upsertTutorialI18n(Long tutorialId, String locale, String title, String summary, String content) {
        if (!StringUtils.hasText(title) && summary == null && content == null) {
            return;
        }
        I18nCmsTutorial existing = i18nTutorialService.getOne(new LambdaQueryWrapper<I18nCmsTutorial>()
                .eq(I18nCmsTutorial::getTutorialId, tutorialId)
                .eq(I18nCmsTutorial::getLocale, locale));
        if (existing == null) {
            if (!StringUtils.hasText(title)) {
                return;
            }
            I18nCmsTutorial row = new I18nCmsTutorial();
            row.setTutorialId(tutorialId);
            row.setLocale(locale);
            row.setTitle(title.trim());
            row.setSummary(summary);
            row.setContent(content);
            i18nTutorialService.save(row);
        } else {
            if (StringUtils.hasText(title)) {
                existing.setTitle(title.trim());
            }
            if (summary != null) {
                existing.setSummary(summary);
            }
            if (content != null) {
                existing.setContent(content);
            }
            i18nTutorialService.update(existing, new LambdaQueryWrapper<I18nCmsTutorial>()
                    .eq(I18nCmsTutorial::getTutorialId, tutorialId)
                    .eq(I18nCmsTutorial::getLocale, locale));
        }
    }

    private void upsertAppDownloadI18n(Long downloadId, String locale, String name, String remark) {
        if (!StringUtils.hasText(name) && remark == null) {
            return;
        }
        I18nCmsAppDownload existing = i18nAppDownloadService.getOne(new LambdaQueryWrapper<I18nCmsAppDownload>()
                .eq(I18nCmsAppDownload::getDownloadId, downloadId)
                .eq(I18nCmsAppDownload::getLocale, locale));
        if (existing == null) {
            if (!StringUtils.hasText(name)) {
                return;
            }
            I18nCmsAppDownload row = new I18nCmsAppDownload();
            row.setDownloadId(downloadId);
            row.setLocale(locale);
            row.setName(name.trim());
            row.setRemark(remark);
            i18nAppDownloadService.save(row);
        } else {
            if (StringUtils.hasText(name)) {
                existing.setName(name.trim());
            }
            if (remark != null) {
                existing.setRemark(remark);
            }
            i18nAppDownloadService.update(existing, new LambdaQueryWrapper<I18nCmsAppDownload>()
                    .eq(I18nCmsAppDownload::getDownloadId, downloadId)
                    .eq(I18nCmsAppDownload::getLocale, locale));
        }
    }

    private void upsertPaymentI18n(Long channelId, String locale, String name, String description) {
        if (!StringUtils.hasText(name) && description == null) {
            return;
        }
        I18nPaymentChannel existing = i18nPaymentChannelService.getOne(new LambdaQueryWrapper<I18nPaymentChannel>()
                .eq(I18nPaymentChannel::getChannelId, channelId)
                .eq(I18nPaymentChannel::getLocale, locale));
        if (existing == null) {
            if (!StringUtils.hasText(name)) {
                return;
            }
            I18nPaymentChannel row = new I18nPaymentChannel();
            row.setChannelId(channelId);
            row.setLocale(locale);
            row.setName(name.trim());
            row.setDescription(description);
            i18nPaymentChannelService.save(row);
        } else {
            if (StringUtils.hasText(name)) {
                existing.setName(name.trim());
            }
            if (description != null) {
                existing.setDescription(description);
            }
            i18nPaymentChannelService.update(existing, new LambdaQueryWrapper<I18nPaymentChannel>()
                    .eq(I18nPaymentChannel::getChannelId, channelId)
                    .eq(I18nPaymentChannel::getLocale, locale));
        }
    }

    private void applyTutorial(CmsTutorial t, AdminTutorialRequest req) {
        if (req.getCategoryId() != null) {
            t.setCategoryId(req.getCategoryId());
        }
        if (req.getSlug() != null) {
            t.setSlug(req.getSlug());
        }
        if (req.getContentType() != null) {
            t.setContentType(req.getContentType());
        }
        String cover = req.getCoverUrl() != null ? req.getCoverUrl() : req.getCoverImageUrl();
        if (cover != null) {
            t.setCoverUrl(cover);
        }
        if (req.getMediaUrl() != null) {
            t.setMediaUrl(req.getMediaUrl());
        }
        if (req.getSortOrder() != null || req.getSort() != null) {
            t.setSortOrder(sortOf(req.getSortOrder(), req.getSort()));
        } else if (t.getSortOrder() == null) {
            t.setSortOrder(0);
        }
        if (req.getStatus() != null) {
            t.setStatus(req.getStatus());
        } else if (t.getStatus() == null) {
            t.setStatus(1);
        }
    }

    private void applyAppDownload(CmsAppDownload d, AdminAppDownloadRequest req) {
        if (req.getPlatform() != null) {
            d.setPlatform(req.getPlatform().trim());
        }
        if (req.getDownloadUrl() != null) {
            d.setDownloadUrl(req.getDownloadUrl().trim());
        }
        if (req.getQrImageUrl() != null) {
            d.setQrImageUrl(req.getQrImageUrl());
        }
        if (req.getSortOrder() != null) {
            d.setSortOrder(req.getSortOrder());
        } else if (d.getSortOrder() == null) {
            d.setSortOrder(0);
        }
        if (req.getStatus() != null) {
            d.setStatus(req.getStatus());
        } else if (d.getStatus() == null) {
            d.setStatus(1);
        }
    }

    private void applyCoupon(Coupon c, AdminCouponRequest req) {
        if (StringUtils.hasText(req.getCode())) {
            c.setCode(req.getCode().trim());
        }
        if (req.getDiscountAmount() != null) {
            c.setDiscountAmount(req.getDiscountAmount());
        }
        if (req.getUseType() != null) {
            c.setUseType(req.getUseType());
        } else if (c.getUseType() == null) {
            c.setUseType("ONCE");
        }
        if (req.getCategoryId() != null) {
            c.setCategoryId(req.getCategoryId());
        }
        if (req.getMinOrderAmount() != null) {
            c.setMinOrderAmount(req.getMinOrderAmount());
        }
        if (req.getStatus() != null) {
            c.setStatus(req.getStatus());
        }
        if (req.getStartsAt() != null) {
            c.setStartsAt(parseDateTime(req.getStartsAt()));
        }
        if (req.getExpiresAt() != null) {
            c.setExpiresAt(parseDateTime(req.getExpiresAt()));
        }
        if (req.getRemark() != null) {
            c.setRemark(req.getRemark());
        }
    }

    private AdminFaqCategoryVO toFaqCategoryVO(CmsFaqCategory c) {
        I18nCmsFaqCategory zh = i18nFaqCategoryService.getOne(new LambdaQueryWrapper<I18nCmsFaqCategory>()
                .eq(I18nCmsFaqCategory::getCategoryId, c.getId()).eq(I18nCmsFaqCategory::getLocale, "zh"));
        I18nCmsFaqCategory en = i18nFaqCategoryService.getOne(new LambdaQueryWrapper<I18nCmsFaqCategory>()
                .eq(I18nCmsFaqCategory::getCategoryId, c.getId()).eq(I18nCmsFaqCategory::getLocale, "en"));
        String nameZh = zh != null ? zh.getName() : null;
        return AdminFaqCategoryVO.builder()
                .id(c.getId())
                .name(nameZh)
                .nameZh(nameZh)
                .nameEn(en != null ? en.getName() : null)
                .sort(c.getSortOrder())
                .sortOrder(c.getSortOrder())
                .status(c.getStatus())
                .build();
    }

    private AdminFaqVO toFaqVO(CmsFaq f) {
        I18nCmsFaq zh = i18nFaqService.getOne(new LambdaQueryWrapper<I18nCmsFaq>()
                .eq(I18nCmsFaq::getFaqId, f.getId()).eq(I18nCmsFaq::getLocale, "zh"));
        I18nCmsFaq en = i18nFaqService.getOne(new LambdaQueryWrapper<I18nCmsFaq>()
                .eq(I18nCmsFaq::getFaqId, f.getId()).eq(I18nCmsFaq::getLocale, "en"));
        String questionZh = zh != null ? zh.getQuestion() : null;
        String answerZh = zh != null ? zh.getAnswer() : null;
        return AdminFaqVO.builder()
                .id(f.getId())
                .categoryId(f.getCategoryId())
                .question(questionZh)
                .questionZh(questionZh)
                .answer(answerZh)
                .answerZh(answerZh)
                .questionEn(en != null ? en.getQuestion() : null)
                .answerEn(en != null ? en.getAnswer() : null)
                .sort(f.getSortOrder())
                .sortOrder(f.getSortOrder())
                .status(f.getStatus())
                .build();
    }

    private AdminTutorialCategoryVO toTutorialCategoryVO(CmsTutorialCategory c) {
        I18nCmsTutorialCategory zh = i18nTutorialCategoryService.getOne(new LambdaQueryWrapper<I18nCmsTutorialCategory>()
                .eq(I18nCmsTutorialCategory::getCategoryId, c.getId()).eq(I18nCmsTutorialCategory::getLocale, "zh"));
        I18nCmsTutorialCategory en = i18nTutorialCategoryService.getOne(new LambdaQueryWrapper<I18nCmsTutorialCategory>()
                .eq(I18nCmsTutorialCategory::getCategoryId, c.getId()).eq(I18nCmsTutorialCategory::getLocale, "en"));
        return AdminTutorialCategoryVO.builder()
                .id(c.getId())
                .name(zh != null ? zh.getName() : null)
                .nameEn(en != null ? en.getName() : null)
                .icon(c.getIcon())
                .sort(c.getSortOrder())
                .sortOrder(c.getSortOrder())
                .status(c.getStatus())
                .build();
    }

    private AdminTutorialVO toTutorialVO(CmsTutorial t) {
        I18nCmsTutorial zh = i18nTutorialService.getOne(new LambdaQueryWrapper<I18nCmsTutorial>()
                .eq(I18nCmsTutorial::getTutorialId, t.getId()).eq(I18nCmsTutorial::getLocale, "zh"));
        I18nCmsTutorial en = i18nTutorialService.getOne(new LambdaQueryWrapper<I18nCmsTutorial>()
                .eq(I18nCmsTutorial::getTutorialId, t.getId()).eq(I18nCmsTutorial::getLocale, "en"));
        String titleZh = zh != null ? zh.getTitle() : null;
        String contentZh = zh != null ? zh.getContent() : null;
        return AdminTutorialVO.builder()
                .id(t.getId())
                .categoryId(t.getCategoryId())
                .slug(t.getSlug())
                .contentType(t.getContentType())
                .title(titleZh)
                .titleZh(titleZh)
                .summary(zh != null ? zh.getSummary() : null)
                .content(contentZh)
                .contentZh(contentZh)
                .titleEn(en != null ? en.getTitle() : null)
                .summaryEn(en != null ? en.getSummary() : null)
                .contentEn(en != null ? en.getContent() : null)
                .coverUrl(t.getCoverUrl())
                .coverImageUrl(t.getCoverUrl())
                .mediaUrl(t.getMediaUrl())
                .sort(t.getSortOrder())
                .sortOrder(t.getSortOrder())
                .status(t.getStatus())
                .build();
    }

    private AdminAppDownloadVO toAppDownloadVO(CmsAppDownload d) {
        I18nCmsAppDownload zh = i18nAppDownloadService.getOne(new LambdaQueryWrapper<I18nCmsAppDownload>()
                .eq(I18nCmsAppDownload::getDownloadId, d.getId()).eq(I18nCmsAppDownload::getLocale, "zh"));
        I18nCmsAppDownload en = i18nAppDownloadService.getOne(new LambdaQueryWrapper<I18nCmsAppDownload>()
                .eq(I18nCmsAppDownload::getDownloadId, d.getId()).eq(I18nCmsAppDownload::getLocale, "en"));
        String nameZh = zh != null ? zh.getName() : d.getPlatform();
        String nameEn = en != null ? en.getName() : null;
        return AdminAppDownloadVO.builder()
                .id(d.getId())
                .platform(d.getPlatform())
                .name(nameZh)
                .titleZh(nameZh)
                .nameEn(nameEn)
                .titleEn(nameEn)
                .downloadUrl(d.getDownloadUrl())
                .qrImageUrl(d.getQrImageUrl())
                .sortOrder(d.getSortOrder())
                .status(d.getStatus())
                .remark(zh != null ? zh.getRemark() : null)
                .build();
    }

    private AdminPaymentMethodVO toPaymentVO(PaymentChannel c) {
        I18nPaymentChannel zh = i18nPaymentChannelService.getOne(new LambdaQueryWrapper<I18nPaymentChannel>()
                .eq(I18nPaymentChannel::getChannelId, c.getId()).eq(I18nPaymentChannel::getLocale, "zh"));
        I18nPaymentChannel en = i18nPaymentChannelService.getOne(new LambdaQueryWrapper<I18nPaymentChannel>()
                .eq(I18nPaymentChannel::getChannelId, c.getId()).eq(I18nPaymentChannel::getLocale, "en"));
        String nameZh = zh != null ? zh.getName() : c.getCode();
        return AdminPaymentMethodVO.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(nameZh)
                .nameZh(nameZh)
                .nameEn(en != null ? en.getName() : null)
                .description(zh != null ? zh.getDescription() : null)
                .descriptionEn(en != null ? en.getDescription() : null)
                .icon(c.getIcon())
                .enabled(c.getEnabled())
                .sortOrder(c.getSortOrder())
                .extraJson(c.getExtraJson())
                .build();
    }

    private AdminCouponVO toCouponVO(Coupon c) {
        long used = couponUsageService.count(new LambdaQueryWrapper<CouponUsage>()
                .eq(CouponUsage::getCouponId, c.getId()));
        return AdminCouponVO.builder()
                .id(c.getId())
                .code(c.getCode())
                .discountAmount(c.getDiscountAmount())
                .useType(c.getUseType())
                .categoryId(c.getCategoryId())
                .status(c.getStatus())
                .minOrderAmount(c.getMinOrderAmount())
                .startsAt(c.getStartsAt() == null ? null : c.getStartsAt().format(FMT))
                .expiresAt(c.getExpiresAt() == null ? null : c.getExpiresAt().format(FMT))
                .remark(c.getRemark())
                .usedCount(used)
                .build();
    }

    private static LocalDateTime parseDateTime(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String v = raw.trim().replace('T', ' ');
        if (v.length() == 10) {
            v = v + " 00:00:00";
        }
        try {
            return LocalDateTime.parse(v, FMT);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(raw.trim());
            } catch (Exception ex) {
                throw new BizException("时间格式无效: " + raw);
            }
        }
    }
}
