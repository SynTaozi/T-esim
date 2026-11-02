package com.tesim.controller;

import com.tesim.common.ApiResponse;
import com.tesim.dto.cms.*;
import com.tesim.service.CmsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CmsController {

    private final CmsAppService cmsAppService;

    @GetMapping("/faq/categories")
    public ApiResponse<List<FaqCategoryVO>> faqCategories() {
        return ApiResponse.ok(cmsAppService.faqCategories());
    }

    @GetMapping("/faq/categories/{id}")
    public ApiResponse<FaqCategoryVO> faqCategory(@PathVariable Long id) {
        return ApiResponse.ok(cmsAppService.faqCategoryById(id));
    }

    @GetMapping("/faq")
    public ApiResponse<List<FaqVO>> faqs(@RequestParam(required = false) Long categoryId) {
        return ApiResponse.ok(cmsAppService.faqs(categoryId));
    }

    @GetMapping("/faq/{id}")
    public ApiResponse<FaqVO> faq(@PathVariable Long id) {
        return ApiResponse.ok(cmsAppService.faqById(id));
    }

    @GetMapping("/tutorials/overview")
    public ApiResponse<TutorialOverviewVO> tutorialOverview() {
        return ApiResponse.ok(cmsAppService.tutorialOverview());
    }

    @GetMapping("/tutorials/categories")
    public ApiResponse<List<TutorialCategoryVO>> tutorialCategories() {
        return ApiResponse.ok(cmsAppService.tutorialCategories());
    }

    @GetMapping("/tutorials")
    public ApiResponse<List<TutorialVO>> tutorials(@RequestParam(required = false) Long categoryId) {
        return ApiResponse.ok(cmsAppService.tutorials(categoryId));
    }

    @GetMapping("/tutorials/{slug}")
    public ApiResponse<TutorialVO> tutorial(@PathVariable String slug) {
        return ApiResponse.ok(cmsAppService.tutorialBySlug(slug));
    }

    @GetMapping("/app-download-qrcodes")
    public ApiResponse<List<AppDownloadVO>> appDownloads() {
        return ApiResponse.ok(cmsAppService.appDownloads());
    }

    @GetMapping("/public/site-display")
    public ApiResponse<SiteDisplayVO> siteDisplay() {
        return ApiResponse.ok(cmsAppService.siteDisplay());
    }

    @GetMapping("/system/theme")
    public ApiResponse<ThemeVO> theme() {
        return ApiResponse.ok(cmsAppService.currentTheme());
    }

    @GetMapping("/payment-methods/enabled")
    public ApiResponse<List<PaymentMethodVO>> paymentMethods() {
        return ApiResponse.ok(cmsAppService.enabledPaymentMethods());
    }
}
