package com.xlj.mall.controller;

import com.xlj.mall.common.ApiResponse;
import com.xlj.mall.common.PageResult;
import com.xlj.mall.dto.ImportResult;
import com.xlj.mall.dto.ProductRequest;
import com.xlj.mall.dto.ProductView;
import com.xlj.mall.dto.StatusRequest;
import com.xlj.mall.service.ProductService;
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

@Tag(name = "03. 商品管理")
@RestController
@RequestMapping("/admin/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "商品分页列表")
    @GetMapping
    public ApiResponse<PageResult<ProductView>> page(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(productService.page(name, categoryId, status, Math.max(page, 1), clamp(size)));
    }

    @Operation(summary = "新增商品")
    @PostMapping
    public ApiResponse<ProductView> create(@Valid @RequestBody ProductRequest request) {
        return ApiResponse.ok(productService.create(request));
    }

    @Operation(summary = "编辑商品")
    @PutMapping("/{id}")
    public ApiResponse<ProductView> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ApiResponse.ok(productService.update(id, request));
    }

    @Operation(summary = "上架 / 下架")
    @PatchMapping("/{id}/status")
    public ApiResponse<ProductView> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return ApiResponse.ok(productService.updateStatus(id, request.status()));
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "下载批量导入模板")
    @GetMapping("/template")
    public ResponseEntity<byte[]> template() {
        return csv("商品导入模板.csv", productService.templateCsv());
    }

    @Operation(summary = "批量导入商品")
    @PostMapping("/import")
    public ApiResponse<ImportResult> importCsv(@RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(productService.importCsv(file));
    }

    private static ResponseEntity<byte[]> csv(String filename, byte[] content) {
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(content);
    }

    private static int clamp(int size) {
        if (size < 1) return 10;
        return Math.min(size, 100);
    }
}
