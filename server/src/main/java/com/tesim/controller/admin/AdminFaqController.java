package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.dto.admin.AdminFaqCategoryRequest;
import com.tesim.dto.admin.AdminFaqCategoryVO;
import com.tesim.dto.admin.AdminFaqRequest;
import com.tesim.dto.admin.AdminFaqVO;
import com.tesim.service.AdminCmsAdminAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/faq")
@RequiredArgsConstructor
public class AdminFaqController {

    private final AdminCmsAdminAppService cmsAppService;

    @GetMapping("/categories")
    public ApiResponse<List<AdminFaqCategoryVO>> categories() {
        return ApiResponse.ok(cmsAppService.listFaqCategories());
    }

    @PostMapping("/categories")
    public ApiResponse<AdminFaqCategoryVO> createCategory(@RequestBody AdminFaqCategoryRequest req) {
        return ApiResponse.ok(cmsAppService.createFaqCategory(req));
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<AdminFaqCategoryVO> updateCategory(@PathVariable Long id, @RequestBody AdminFaqCategoryRequest req) {
        return ApiResponse.ok(cmsAppService.updateFaqCategory(id, req));
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        cmsAppService.deleteFaqCategory(id);
        return ApiResponse.ok(null);
    }

    @GetMapping
    public ApiResponse<List<AdminFaqVO>> list(@RequestParam(required = false) Long categoryId) {
        return ApiResponse.ok(cmsAppService.listFaqs(categoryId));
    }

    @PostMapping
    public ApiResponse<AdminFaqVO> create(@RequestBody AdminFaqRequest req) {
        return ApiResponse.ok(cmsAppService.createFaq(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminFaqVO> update(@PathVariable Long id, @RequestBody AdminFaqRequest req) {
        return ApiResponse.ok(cmsAppService.updateFaq(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        cmsAppService.deleteFaq(id);
        return ApiResponse.ok(null);
    }
}
