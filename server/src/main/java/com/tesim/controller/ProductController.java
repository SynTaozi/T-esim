package com.tesim.controller;

import com.tesim.common.ApiResponse;
import com.tesim.dto.product.CategoryVO;
import com.tesim.dto.product.PricingTierVO;
import com.tesim.dto.product.ProductListVO;
import com.tesim.dto.product.ProductVO;
import com.tesim.dto.product.RegionCountsVO;
import com.tesim.service.ProductCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductCatalogService catalogService;

    @GetMapping("/products")
    public ApiResponse<ProductListVO> list(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long itemType,
            @RequestParam(required = false) Long subCategoryId) {
        return ApiResponse.ok(catalogService.listProducts(region, page, size, itemType, subCategoryId));
    }

    @GetMapping("/products/search")
    public ApiResponse<ProductListVO> search(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long itemType,
            @RequestParam(required = false) Long subCategoryId) {
        return ApiResponse.ok(catalogService.search(keyword, page, size, itemType, subCategoryId));
    }

    @GetMapping("/products/featured")
    public ApiResponse<List<ProductVO>> featured(@RequestParam(required = false) Integer size) {
        return ApiResponse.ok(catalogService.featured(size));
    }

    @GetMapping("/products/regions")
    public ApiResponse<List<String>> regions() {
        return ApiResponse.ok(catalogService.regions());
    }

    @GetMapping("/products/region-counts")
    public ApiResponse<RegionCountsVO> regionCounts() {
        return ApiResponse.ok(catalogService.regionCounts());
    }

    @GetMapping("/products/{id}")
    public ApiResponse<ProductVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(catalogService.detail(id));
    }

    @GetMapping("/products/{id}/pricing-tiers")
    public ApiResponse<List<PricingTierVO>> tiers(@PathVariable Long id) {
        return ApiResponse.ok(catalogService.pricingTiers(id));
    }

    @GetMapping("/product-categories")
    public ApiResponse<List<CategoryVO>> categories() {
        return ApiResponse.ok(catalogService.categories());
    }

    @GetMapping("/product-subcategories")
    public ApiResponse<List<CategoryVO>> subcategories(@RequestParam(required = false) Long categoryId) {
        return ApiResponse.ok(catalogService.subcategories(categoryId));
    }
}
