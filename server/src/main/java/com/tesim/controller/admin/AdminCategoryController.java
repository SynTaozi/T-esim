package com.tesim.controller.admin;

import com.tesim.common.ApiResponse;
import com.tesim.dto.admin.AdminCategoryRequest;
import com.tesim.dto.admin.AdminCategoryVO;
import com.tesim.service.AdminCategoryAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/product-categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminCategoryAppService categoryAppService;

    @GetMapping
    public ApiResponse<List<AdminCategoryVO>> list(@RequestParam(required = false) Long parentId) {
        if (parentId != null) {
            return ApiResponse.ok(categoryAppService.listSubs(parentId));
        }
        return ApiResponse.ok(categoryAppService.listRoots());
    }

    @GetMapping("/enabled")
    public ApiResponse<List<AdminCategoryVO>> enabled() {
        return ApiResponse.ok(categoryAppService.listEnabledRoots());
    }

    @GetMapping("/tree")
    public ApiResponse<List<AdminCategoryVO>> tree() {
        return ApiResponse.ok(categoryAppService.tree());
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminCategoryVO> get(@PathVariable Long id) {
        return ApiResponse.ok(categoryAppService.get(id));
    }

    @PostMapping
    public ApiResponse<AdminCategoryVO> create(@RequestBody AdminCategoryRequest req) {
        return ApiResponse.ok(categoryAppService.createRoot(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminCategoryVO> update(@PathVariable Long id, @RequestBody AdminCategoryRequest req) {
        return ApiResponse.ok(categoryAppService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryAppService.delete(id);
        return ApiResponse.ok("已删除", null);
    }
}
