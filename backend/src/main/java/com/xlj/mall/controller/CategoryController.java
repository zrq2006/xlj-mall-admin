package com.xlj.mall.controller;

import com.xlj.mall.common.ApiResponse;
import com.xlj.mall.common.PageResult;
import com.xlj.mall.dto.CategoryRequest;
import com.xlj.mall.dto.CategoryView;
import com.xlj.mall.dto.StatusRequest;
import com.xlj.mall.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.util.List;

@Tag(name = "04. 分类管理")
@RestController
@RequestMapping("/admin/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "分类分页列表")
    @GetMapping
    public ApiResponse<PageResult<CategoryView>> page(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(categoryService.page(name, status, Math.max(page, 1), Math.min(Math.max(size, 1), 100)));
    }

    @Operation(summary = "全部分类（下拉选项）")
    @GetMapping("/all")
    public ApiResponse<List<CategoryView>> all() {
        return ApiResponse.ok(categoryService.listAll());
    }

    @Operation(summary = "新增分类")
    @PostMapping
    public ApiResponse<CategoryView> create(@Valid @RequestBody CategoryRequest request) {
        return ApiResponse.ok(categoryService.create(request));
    }

    @Operation(summary = "编辑分类")
    @PutMapping("/{id}")
    public ApiResponse<CategoryView> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.ok(categoryService.update(id, request));
    }

    @Operation(summary = "显示 / 隐藏")
    @PatchMapping("/{id}/status")
    public ApiResponse<CategoryView> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return ApiResponse.ok(categoryService.updateStatus(id, request.status()));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.ok();
    }
}
