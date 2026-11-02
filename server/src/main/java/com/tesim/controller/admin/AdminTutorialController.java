package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.dto.admin.AdminTutorialCategoryRequest;
import com.tesim.dto.admin.AdminTutorialCategoryVO;
import com.tesim.dto.admin.AdminTutorialRequest;
import com.tesim.dto.admin.AdminTutorialVO;
import com.tesim.service.AdminCmsAdminAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/tutorials")
@RequiredArgsConstructor
public class AdminTutorialController {

    private final AdminCmsAdminAppService cmsAppService;

    @GetMapping("/categories")
    public ApiResponse<List<AdminTutorialCategoryVO>> categories() {
        return ApiResponse.ok(cmsAppService.listTutorialCategories());
    }

    @PostMapping("/categories")
    public ApiResponse<AdminTutorialCategoryVO> createCategory(@RequestBody AdminTutorialCategoryRequest req) {
        return ApiResponse.ok(cmsAppService.createTutorialCategory(req));
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<AdminTutorialCategoryVO> updateCategory(@PathVariable Long id,
                                                               @RequestBody AdminTutorialCategoryRequest req) {
        return ApiResponse.ok(cmsAppService.updateTutorialCategory(id, req));
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        cmsAppService.deleteTutorialCategory(id);
        return ApiResponse.ok(null);
    }

    @GetMapping
    public ApiResponse<List<AdminTutorialVO>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String contentType) {
        return ApiResponse.ok(cmsAppService.listTutorials(categoryId, contentType));
    }

    @PostMapping
    public ApiResponse<AdminTutorialVO> create(@RequestBody AdminTutorialRequest req) {
        return ApiResponse.ok(cmsAppService.createTutorial(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminTutorialVO> update(@PathVariable Long id, @RequestBody AdminTutorialRequest req) {
        return ApiResponse.ok(cmsAppService.updateTutorial(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        cmsAppService.deleteTutorial(id);
        return ApiResponse.ok(null);
    }
}
