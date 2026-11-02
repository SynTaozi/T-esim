package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.CmsAppService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tesim.common.BizException;
import com.tesim.dto.cms.*;
import com.tesim.entity.*;
import com.tesim.util.LocaleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CmsAppServiceImpl implements CmsAppService {

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
    private final CmsSiteSettingService siteSettingService;
    private final SysThemeService themeService;
    private final PaymentChannelService paymentChannelService;
    private final I18nPaymentChannelService i18nPaymentChannelService;

    public List<FaqCategoryVO> faqCategories() {
        String locale = LocaleHelper.current();
        return faqCategoryService.list(new LambdaQueryWrapper<CmsFaqCategory>()
                        .eq(CmsFaqCategory::getStatus, 1)
                        .orderByAsc(CmsFaqCategory::getSortOrder))
                .stream().map(c -> {
                    I18nCmsFaqCategory i18n = i18nFaqCategoryService.getOne(new LambdaQueryWrapper<I18nCmsFaqCategory>()
                            .eq(I18nCmsFaqCategory::getCategoryId, c.getId())
                            .eq(I18nCmsFaqCategory::getLocale, locale));
                    return FaqCategoryVO.builder()
                            .id(c.getId())
                            .name(i18n != null ? i18n.getName() : ("Category " + c.getId()))
                            .sortOrder(c.getSortOrder())
                            .build();
                }).toList();
    }

    public List<FaqVO> faqs(Long categoryId) {
        String locale = LocaleHelper.current();
        LambdaQueryWrapper<CmsFaq> qw = new LambdaQueryWrapper<CmsFaq>()
                .eq(CmsFaq::getStatus, 1)
                .orderByAsc(CmsFaq::getSortOrder);
        if (categoryId != null) {
            qw.eq(CmsFaq::getCategoryId, categoryId);
        }
        return faqService.list(qw).stream().map(f -> {
            I18nCmsFaq i18n = i18nFaqService.getOne(new LambdaQueryWrapper<I18nCmsFaq>()
                    .eq(I18nCmsFaq::getFaqId, f.getId())
                    .eq(I18nCmsFaq::getLocale, locale));
            return FaqVO.builder()
                    .id(f.getId())
                    .categoryId(f.getCategoryId())
                    .question(i18n != null ? i18n.getQuestion() : "")
                    .answer(i18n != null ? i18n.getAnswer() : "")
                    .sortOrder(f.getSortOrder())
                    .build();
        }).toList();
    }

    public FaqVO faqById(Long id) {
        return faqs(null).stream().filter(m -> Objects.equals(m.getId(), id)).findFirst()
                .orElseThrow(() -> new BizException("FAQ 不存在"));
    }

    public FaqCategoryVO faqCategoryById(Long id) {
        return faqCategories().stream().filter(m -> Objects.equals(m.getId(), id)).findFirst()
                .orElse(null);
    }

    public List<TutorialCategoryVO> tutorialCategories() {
        String locale = LocaleHelper.current();
        return tutorialCategoryService.list(new LambdaQueryWrapper<CmsTutorialCategory>()
                        .eq(CmsTutorialCategory::getStatus, 1)
                        .orderByAsc(CmsTutorialCategory::getSortOrder))
                .stream().map(c -> {
                    I18nCmsTutorialCategory i18n = i18nTutorialCategoryService.getOne(new LambdaQueryWrapper<I18nCmsTutorialCategory>()
                            .eq(I18nCmsTutorialCategory::getCategoryId, c.getId())
                            .eq(I18nCmsTutorialCategory::getLocale, locale));
                    return TutorialCategoryVO.builder()
                            .id(c.getId())
                            .name(i18n != null ? i18n.getName() : ("Category " + c.getId()))
                            .icon(c.getIcon())
                            .sortOrder(c.getSortOrder())
                            .build();
                }).toList();
    }

    public List<TutorialVO> tutorials(Long categoryId) {
        String locale = LocaleHelper.current();
        LambdaQueryWrapper<CmsTutorial> qw = new LambdaQueryWrapper<CmsTutorial>()
                .eq(CmsTutorial::getStatus, 1)
                .orderByAsc(CmsTutorial::getSortOrder);
        if (categoryId != null) {
            qw.eq(CmsTutorial::getCategoryId, categoryId);
        }
        return tutorialService.list(qw).stream().map(t -> toTutorial(t, locale, false)).toList();
    }

    public TutorialVO tutorialBySlug(String slug) {
        CmsTutorial t = tutorialService.getOne(new LambdaQueryWrapper<CmsTutorial>()
                .eq(CmsTutorial::getSlug, slug)
                .eq(CmsTutorial::getStatus, 1));
        if (t == null) {
            throw new BizException("教程不存在");
        }
        return toTutorial(t, LocaleHelper.current(), true);
    }

    public TutorialOverviewVO tutorialOverview() {
        return TutorialOverviewVO.builder()
                .categories(tutorialCategories())
                .tutorials(tutorials(null))
                .build();
    }

    public List<AppDownloadVO> appDownloads() {
        String locale = LocaleHelper.current();
        return appDownloadService.list(new LambdaQueryWrapper<CmsAppDownload>()
                        .eq(CmsAppDownload::getStatus, 1)
                        .orderByAsc(CmsAppDownload::getSortOrder))
                .stream().map(d -> {
                    I18nCmsAppDownload i18n = i18nAppDownloadService.getOne(new LambdaQueryWrapper<I18nCmsAppDownload>()
                            .eq(I18nCmsAppDownload::getDownloadId, d.getId())
                            .eq(I18nCmsAppDownload::getLocale, locale));
                    return AppDownloadVO.builder()
                            .id(d.getId())
                            .platform(d.getPlatform())
                            .downloadUrl(d.getDownloadUrl())
                            .qrImageUrl(d.getQrImageUrl())
                            .name(i18n != null ? i18n.getName() : d.getPlatform())
                            .remark(i18n != null ? i18n.getRemark() : null)
                            .build();
                }).toList();
    }

    public SiteDisplayVO siteDisplay() {
        CmsSiteSetting s = siteSettingService.getOne(new LambdaQueryWrapper<CmsSiteSetting>()
                .eq(CmsSiteSetting::getCode, "default")
                .last("LIMIT 1"));
        if (s == null) {
            return SiteDisplayVO.builder()
                    .defaultShippingFee(BigDecimal.ZERO)
                    .build();
        }
        return SiteDisplayVO.builder()
                .loginBannerUrl(s.getLoginBannerUrl())
                .registerBannerUrl(s.getRegisterBannerUrl())
                .homeHeroImageUrl(s.getHomeHeroImageUrl())
                .defaultShippingFee(s.getDefaultShippingFee())
                .build();
    }

    public ThemeVO currentTheme() {
        SysTheme theme = themeService.getOne(new LambdaQueryWrapper<SysTheme>().last("LIMIT 1"));
        if (theme == null) {
            return ThemeVO.builder()
                    .themeId("apple")
                    .themeName("Apple")
                    .build();
        }
        return ThemeVO.builder()
                .themeId(theme.getThemeKey())
                .themeName(theme.getThemeName())
                .code(theme.getCode())
                .build();
    }

    public List<PaymentMethodVO> enabledPaymentMethods() {
        String locale = LocaleHelper.current();
        return paymentChannelService.list(new LambdaQueryWrapper<PaymentChannel>()
                        .eq(PaymentChannel::getEnabled, true)
                        .orderByAsc(PaymentChannel::getSortOrder))
                .stream()
                .filter(c -> !"BALANCE".equalsIgnoreCase(c.getCode()))
                .filter(c -> !"ANTOM_ALIPAY".equalsIgnoreCase(c.getCode()))
                .map(c -> {
                    I18nPaymentChannel i18n = i18nPaymentChannelService.getOne(new LambdaQueryWrapper<I18nPaymentChannel>()
                            .eq(I18nPaymentChannel::getChannelId, c.getId())
                            .eq(I18nPaymentChannel::getLocale, locale));
                    return PaymentMethodVO.builder()
                            .id(c.getId())
                            .code(c.getCode())
                            .name(i18n != null ? i18n.getName() : c.getCode())
                            .description(i18n != null ? i18n.getDescription() : null)
                            .icon(c.getIcon())
                            .sortOrder(c.getSortOrder())
                            .build();
                }).toList();
    }

    private TutorialVO toTutorial(CmsTutorial t, String locale, boolean withContent) {
        I18nCmsTutorial i18n = i18nTutorialService.getOne(new LambdaQueryWrapper<I18nCmsTutorial>()
                .eq(I18nCmsTutorial::getTutorialId, t.getId())
                .eq(I18nCmsTutorial::getLocale, locale));
        TutorialVO.TutorialVOBuilder builder = TutorialVO.builder()
                .id(t.getId())
                .categoryId(t.getCategoryId())
                .slug(t.getSlug())
                .contentType(t.getContentType())
                .coverUrl(t.getCoverUrl())
                .mediaUrl(t.getMediaUrl())
                .sortOrder(t.getSortOrder())
                .title(i18n != null ? i18n.getTitle() : t.getSlug())
                .summary(i18n != null ? i18n.getSummary() : null);
        if (withContent) {
            builder.content(i18n != null ? i18n.getContent() : null);
        }
        return builder.build();
    }
}
