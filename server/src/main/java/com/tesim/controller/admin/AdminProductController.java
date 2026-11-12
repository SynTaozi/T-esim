package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.AdminProductRequest;
import com.tesim.dto.admin.AdminProductVO;
import com.tesim.dto.admin.AdminSkuRequest;
import com.tesim.dto.admin.AdminSkuVO;
import com.tesim.dto.admin.IdListRequest;
import com.tesim.dto.admin.SortOrderItem;
import com.tesim.service.AdminProductAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductAppService productAppService;

    @GetMapping
    public ApiResponse<PageResult<AdminProductVO>> page(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer itemType,
            @RequestParam(required = false) Integer isPhysicalCard,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(required = false) Long subcategoryId,
            @RequestParam(required = false) Long categoryId) {
        Long sub = subCategoryId != null ? subCategoryId : subcategoryId;
        return ApiResponse.ok(productAppService.page(page, size, name, status, itemType, isPhysicalCard, sub, categoryId, keyword));
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        PageResult<AdminProductVO> page = productAppService.page(0, 1, null, null, null, null, null, null, null);
        Map<String, Object> map = new HashMap<>();
        map.put("total", page.getTotalElements());
        return ApiResponse.ok(map);
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminProductVO> get(@PathVariable Long id) {
        return ApiResponse.ok(productAppService.get(id));
    }

    @PostMapping
    public ApiResponse<AdminProductVO> create(@RequestBody AdminProductRequest req) {
        return ApiResponse.ok(productAppService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminProductVO> update(@PathVariable Long id, @RequestBody AdminProductRequest req) {
        return ApiResponse.ok(productAppService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productAppService.delete(id);
        return ApiResponse.ok("已删除", null);
    }

    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDelete(@RequestBody IdListRequest req) {
        productAppService.batchDelete(req);
        return ApiResponse.ok("批量删除成功", null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> patchStatus(@PathVariable Long id, @RequestParam Integer status) {
        productAppService.patchStatus(id, status);
        return ApiResponse.ok("状态已更新", null);
    }

    @PutMapping("/batch-status")
    public ApiResponse<Void> batchStatus(@RequestBody IdListRequest req) {
        productAppService.batchStatus(req);
        return ApiResponse.ok("批量状态已更新", null);
    }

    @PutMapping("/sort-order")
    public ApiResponse<Void> sortOrder(@RequestBody List<SortOrderItem> items) {
        productAppService.batchSort(items);
        return ApiResponse.ok("排序已更新", null);
    }

    @PutMapping("/batch-update")
    public ApiResponse<Void> batchUpdate(@RequestBody List<AdminProductRequest> items) {
        productAppService.batchUpdate(items);
        return ApiResponse.ok("批量更新完成", null);
    }

    @GetMapping("/{id}/skus")
    public ApiResponse<List<AdminSkuVO>> skus(@PathVariable Long id) {
        return ApiResponse.ok(productAppService.listSkus(id));
    }

    @GetMapping("/{id}/pricing-tiers")
    public ApiResponse<List<AdminSkuVO>> pricingTiers(@PathVariable Long id) {
        return ApiResponse.ok(productAppService.listSkus(id));
    }

    @PostMapping("/{id}/skus")
    public ApiResponse<AdminSkuVO> createSku(@PathVariable Long id, @RequestBody AdminSkuRequest req) {
        return ApiResponse.ok(productAppService.createSku(id, req));
    }

    @PostMapping("/{id}/pricing-tiers")
    public ApiResponse<AdminSkuVO> createPricingTier(@PathVariable Long id, @RequestBody AdminSkuRequest req) {
        return ApiResponse.ok(productAppService.createSku(id, req));
    }

    @PutMapping("/{id}/skus/{skuId}")
    public ApiResponse<AdminSkuVO> updateSku(@PathVariable Long id, @PathVariable Long skuId,
                                             @RequestBody AdminSkuRequest req) {
        return ApiResponse.ok(productAppService.updateSku(id, skuId, req));
    }

    @PutMapping("/{id}/pricing-tiers/{skuId}")
    public ApiResponse<AdminSkuVO> updatePricingTier(@PathVariable Long id, @PathVariable Long skuId,
                                                     @RequestBody AdminSkuRequest req) {
        return ApiResponse.ok(productAppService.updateSku(id, skuId, req));
    }

    @DeleteMapping("/{id}/skus/{skuId}")
    public ApiResponse<Void> deleteSku(@PathVariable Long id, @PathVariable Long skuId) {
        productAppService.deleteSku(id, skuId);
        return ApiResponse.ok("已删除", null);
    }

    @DeleteMapping("/{id}/pricing-tiers/{skuId}")
    public ApiResponse<Void> deletePricingTier(@PathVariable Long id, @PathVariable Long skuId) {
        productAppService.deleteSku(id, skuId);
        return ApiResponse.ok("已删除", null);
    }

    @PutMapping("/{id}/skus/batch")
    public ApiResponse<List<AdminSkuVO>> batchSkus(@PathVariable Long id, @RequestBody List<AdminSkuRequest> reqs) {
        return ApiResponse.ok(productAppService.batchSaveSkus(id, reqs));
    }

    @PutMapping("/{id}/pricing-tiers/batch")
    public ApiResponse<List<AdminSkuVO>> batchPricingTiers(@PathVariable Long id, @RequestBody List<AdminSkuRequest> reqs) {
        return ApiResponse.ok(productAppService.batchSaveSkus(id, reqs));
    }
}
