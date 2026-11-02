package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.AdminConfigAppService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tesim.common.BizException;
import com.tesim.dto.admin.AdminPaymentMethodRequest;
import com.tesim.dto.admin.AdminPaymentMethodVO;
import com.tesim.dto.admin.AdminSiteDisplayRequest;
import com.tesim.dto.admin.AdminThemeRequest;
import com.tesim.dto.cms.SiteDisplayVO;
import com.tesim.dto.cms.ThemeVO;
import com.tesim.entity.CmsSiteSetting;
import com.tesim.entity.I18nPaymentChannel;
import com.tesim.entity.PaymentChannel;
import com.tesim.entity.SysTheme;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminConfigAppServiceImpl implements AdminConfigAppService {

    private final PaymentChannelService paymentChannelService;
    private final I18nPaymentChannelService i18nPaymentChannelService;
    private final CmsSiteSettingService siteSettingService;
    private final SysThemeService themeService;

    // -------- payment methods --------

    public List<AdminPaymentMethodVO> paymentMethods(Boolean enabledOnly) {
        LambdaQueryWrapper<PaymentChannel> qw = new LambdaQueryWrapper<PaymentChannel>()
                .orderByAsc(PaymentChannel::getSortOrder);
        if (Boolean.TRUE.equals(enabledOnly)) {
            qw.eq(PaymentChannel::getEnabled, true);
        }
        return paymentChannelService.list(qw).stream().map(this::toPay).toList();
    }

    @Transactional
    public AdminPaymentMethodVO savePaymentMethod(AdminPaymentMethodRequest req) {
        if (req.getId() != null) {
            return updatePaymentMethod(req.getId(), req);
        }
        if (!StringUtils.hasText(req.getCode())) {
            throw new BizException("支付方式编码不能为空");
        }
        PaymentChannel c = new PaymentChannel();
        c.setCode(req.getCode().trim().toUpperCase());
        c.setIcon(req.getIcon());
        c.setEnabled(req.getEnabled() == null || req.getEnabled());
        c.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        c.setExtraJson(req.getExtraJson());
        paymentChannelService.save(c);
        upsertPayI18n(c.getId(), req);
        return toPay(c);
    }

    @Transactional
    public AdminPaymentMethodVO updatePaymentMethod(Long id, AdminPaymentMethodRequest req) {
        PaymentChannel c = paymentChannelService.getById(id);
        if (c == null) {
            throw new BizException("支付方式不存在");
        }
        if (StringUtils.hasText(req.getCode())) {
            c.setCode(req.getCode().trim().toUpperCase());
        }
        if (req.getIcon() != null) {
            c.setIcon(req.getIcon());
        }
        if (req.getEnabled() != null) {
            c.setEnabled(req.getEnabled());
        }
        if (req.getSortOrder() != null) {
            c.setSortOrder(req.getSortOrder());
        }
        if (req.getExtraJson() != null) {
            c.setExtraJson(req.getExtraJson());
        }
        paymentChannelService.updateById(c);
        upsertPayI18n(id, req);
        return toPay(c);
    }

    @Transactional
    public AdminPaymentMethodVO updatePaymentEnabled(Long id, Boolean enabled) {
        PaymentChannel c = paymentChannelService.getById(id);
        if (c == null) {
            throw new BizException("支付方式不存在");
        }
        c.setEnabled(Boolean.TRUE.equals(enabled));
        paymentChannelService.updateById(c);
        return toPay(c);
    }

    @Transactional
    public void deletePaymentMethod(Long id) {
        if (paymentChannelService.getById(id) == null) {
            throw new BizException("支付方式不存在");
        }
        i18nPaymentChannelService.remove(new LambdaQueryWrapper<I18nPaymentChannel>()
                .eq(I18nPaymentChannel::getChannelId, id));
        paymentChannelService.removeById(id);
    }

    // -------- site display --------

    public SiteDisplayVO siteDisplay() {
        CmsSiteSetting s = defaultSite();
        if (s == null) {
            return SiteDisplayVO.builder().defaultShippingFee(BigDecimal.ZERO).build();
        }
        return SiteDisplayVO.builder()
                .loginBannerUrl(s.getLoginBannerUrl())
                .registerBannerUrl(s.getRegisterBannerUrl())
                .homeHeroImageUrl(s.getHomeHeroImageUrl())
                .defaultShippingFee(s.getDefaultShippingFee())
                .build();
    }

    @Transactional
    public SiteDisplayVO updateSiteDisplay(AdminSiteDisplayRequest req) {
        CmsSiteSetting s = defaultSite();
        if (s == null) {
            s = new CmsSiteSetting();
            s.setCode("default");
            siteSettingService.save(s);
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
        siteSettingService.updateById(s);
        return siteDisplay();
    }

    // -------- theme --------

    public ThemeVO currentTheme() {
        SysTheme theme = themeService.getOne(new LambdaQueryWrapper<SysTheme>().last("LIMIT 1"));
        if (theme == null) {
            return ThemeVO.builder().themeId("apple").themeName("Apple").build();
        }
        return ThemeVO.builder()
                .themeId(theme.getThemeKey())
                .themeName(theme.getThemeName())
                .code(theme.getCode())
                .build();
    }

    @Transactional
    public ThemeVO setTheme(AdminThemeRequest req) {
        if (!StringUtils.hasText(req.getThemeId())) {
            throw new BizException("主题 ID 不能为空");
        }
        SysTheme theme = themeService.getOne(new LambdaQueryWrapper<SysTheme>().last("LIMIT 1"));
        if (theme == null) {
            theme = new SysTheme();
            theme.setCode("default");
            theme.setThemeKey(req.getThemeId().trim());
            theme.setThemeName(StringUtils.hasText(req.getThemeName()) ? req.getThemeName() : req.getThemeId());
            themeService.save(theme);
        } else {
            theme.setThemeKey(req.getThemeId().trim());
            if (StringUtils.hasText(req.getThemeName())) {
                theme.setThemeName(req.getThemeName());
            }
            themeService.updateById(theme);
        }
        return currentTheme();
    }

    private void upsertPayI18n(Long id, AdminPaymentMethodRequest req) {
        upsertPayLocale(id, "zh", req.getName(), req.getDescription());
        upsertPayLocale(id, "en", req.getNameEn(), req.getDescriptionEn());
    }

    private void upsertPayLocale(Long id, String locale, String name, String description) {
        if (!StringUtils.hasText(name) && description == null) {
            return;
        }
        I18nPaymentChannel existing = i18nPaymentChannelService.getOne(new LambdaQueryWrapper<I18nPaymentChannel>()
                .eq(I18nPaymentChannel::getChannelId, id).eq(I18nPaymentChannel::getLocale, locale));
        if (existing == null) {
            if (!StringUtils.hasText(name)) {
                return;
            }
            I18nPaymentChannel row = new I18nPaymentChannel();
            row.setChannelId(id);
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
                    .eq(I18nPaymentChannel::getChannelId, id).eq(I18nPaymentChannel::getLocale, locale));
        }
    }

    private AdminPaymentMethodVO toPay(PaymentChannel c) {
        I18nPaymentChannel zh = i18nPaymentChannelService.getOne(new LambdaQueryWrapper<I18nPaymentChannel>()
                .eq(I18nPaymentChannel::getChannelId, c.getId()).eq(I18nPaymentChannel::getLocale, "zh"));
        I18nPaymentChannel en = i18nPaymentChannelService.getOne(new LambdaQueryWrapper<I18nPaymentChannel>()
                .eq(I18nPaymentChannel::getChannelId, c.getId()).eq(I18nPaymentChannel::getLocale, "en"));
        return AdminPaymentMethodVO.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(zh != null ? zh.getName() : c.getCode())
                .nameEn(en != null ? en.getName() : null)
                .description(zh != null ? zh.getDescription() : null)
                .descriptionEn(en != null ? en.getDescription() : null)
                .icon(c.getIcon())
                .enabled(c.getEnabled())
                .sortOrder(c.getSortOrder())
                .extraJson(c.getExtraJson())
                .build();
    }

    private CmsSiteSetting defaultSite() {
        return siteSettingService.getOne(new LambdaQueryWrapper<CmsSiteSetting>()
                .eq(CmsSiteSetting::getCode, "default").last("LIMIT 1"));
    }
}
