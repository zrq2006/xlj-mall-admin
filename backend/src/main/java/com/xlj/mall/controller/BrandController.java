package com.xlj.mall.controller;

import com.xlj.mall.common.ApiResponse;
import com.xlj.mall.common.PageResult;
import com.xlj.mall.dto.BrandRequest;
import com.xlj.mall.dto.BrandView;
import com.xlj.mall.dto.ImportResult;
import com.xlj.mall.dto.StatusRequest;
import com.xlj.mall.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "05. 品牌管理")
@RestController
@RequestMapping("/admin/brands")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @Operation(summary = "品牌分页列表")
    @GetMapping
    public ApiResponse<PageResult<BrandView>> page(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(brandService.page(name, status, Math.max(page, 1), Math.min(Math.max(size, 1), 100)));
    }

    @Operation(summary = "全部品牌（下拉选项）")
    @GetMapping("/all")
    public ApiResponse<List<BrandView>> all() {
        return ApiResponse.ok(brandService.listAll());
    }

    @Operation(summary = "新增品牌")
    @PostMapping
    public ApiResponse<BrandView> create(@Valid @RequestBody BrandRequest request) {
        return ApiResponse.ok(brandService.create(request));
    }

    @Operation(summary = "编辑品牌（仅创建者可操作）")
    @PutMapping("/{id}")
    public ApiResponse<BrandView> update(@PathVariable Long id, @Valid @RequestBody BrandRequest request) {
        return ApiResponse.ok(brandService.update(id, request));
    }

    @Operation(summary = "显示 / 隐藏")
    @PatchMapping("/{id}/status")
    public ApiResponse<BrandView> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return ApiResponse.ok(brandService.updateStatus(id, request.status()));
    }

    @Operation(summary = "删除品牌（仅创建者可操作）")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        brandService.delete(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "下载批量导入模板")
    @GetMapping("/template")
    public ResponseEntity<byte[]> template() {
        String encoded = URLEncoder.encode("品牌导入模板.csv", StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(brandService.templateCsv());
    }

    @Operation(summary = "批量导入品牌")
    @PostMapping("/import")
    public ApiResponse<ImportResult> importCsv(@RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(brandService.importCsv(file));
    }
}
