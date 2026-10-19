package com.xlj.mall.service;

import com.xlj.mall.common.BusinessException;
import com.xlj.mall.common.PageResult;
import com.xlj.mall.dto.ImportResult;
import com.xlj.mall.dto.ProductRequest;
import com.xlj.mall.dto.ProductView;
import com.xlj.mall.entity.Brand;
import com.xlj.mall.entity.Category;
import com.xlj.mall.entity.Product;
import com.xlj.mall.repository.BrandRepository;
import com.xlj.mall.repository.CategoryRepository;
import com.xlj.mall.repository.ProductRepository;
import com.xlj.mall.security.CurrentAdmin;
import com.xlj.mall.support.CsvUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final int MAX_ERRORS = 10;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          BrandRepository brandRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<ProductView> page(String name, Long categoryId, Integer status, int page, int size) {
        Page<Product> result = productRepository.search(blankToNull(name), categoryId, status,
                PageRequest.of(page - 1, size));
        Map<Long, String> categoryNames = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        Map<Long, String> brandNames = brandRepository.findAll().stream()
                .collect(Collectors.toMap(Brand::getId, Brand::getName));
        List<ProductView> list = result.getContent().stream()
                .map(product -> toView(product, categoryNames, brandNames))
                .toList();
        return new PageResult<>(list, result.getTotalElements(), page, size);
    }

    @Transactional
    public ProductView create(ProductRequest request) {
        validateReferences(request);
        Product product = new Product();
        apply(product, request);
        product.setCreatedBy(CurrentAdmin.username());
        return toView(productRepository.save(product), null, null);
    }

    @Transactional
    public ProductView update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "商品不存在或已被删除"));
        validateReferences(request);
        apply(product, request);
        return toView(productRepository.save(product), null, null);
    }

    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "商品不存在或已被删除"));
        productRepository.delete(product);
    }

    @Transactional
    public ProductView updateStatus(Long id, Integer status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "商品不存在或已被删除"));
        product.setStatus(status);
        Map<Long, String> categoryNames = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        Map<Long, String> brandNames = brandRepository.findAll().stream()
                .collect(Collectors.toMap(Brand::getId, Brand::getName));
        return toView(productRepository.save(product), categoryNames, brandNames);
    }

    public byte[] templateCsv() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"商品名称", "分类", "品牌", "售价", "同行价", "库存", "销量", "商品图片", "状态", "描述"});
        rows.add(new String[]{"示例商品", "日用百货", "古驰", "99.00", "89.00", "100", "0", "", "上架",
                "这里填写商品卖点，支持逗号、引号等特殊字符"});
        return ("\uFEFF" + CsvUtil.build(rows)).getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public ImportResult importCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要导入的 CSV 文件");
        }
        List<String[]> rows;
        try {
            rows = CsvUtil.parse(new String(file.getBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new BusinessException("文件读取失败，请重新上传");
        }
        if (rows.size() <= 1) {
            throw new BusinessException("文件中没有可导入的数据");
        }
        Map<String, Long> categoryIds = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getName, Category::getId, (a, b) -> a));
        Map<String, Long> brandIds = brandRepository.findAll().stream()
                .collect(Collectors.toMap(Brand::getName, Brand::getId, (a, b) -> a));
        List<String> errors = new ArrayList<>();
        int success = 0;
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            String name = BrandService.cell(row, 0);
            if (name.isEmpty()) {
                addError(errors, "第 " + (i + 1) + " 行：商品名称不能为空");
                continue;
            }
            String categoryName = BrandService.cell(row, 1);
            Long categoryId = categoryIds.get(categoryName);
            if (categoryId == null) {
                addError(errors, "第 " + (i + 1) + " 行：分类「" + categoryName + "」不存在，请先在分类管理中创建");
                continue;
            }
            String brandName = BrandService.cell(row, 2);
            Long brandId = brandName.isEmpty() ? null : brandIds.get(brandName);
            if (brandId == null && !brandName.isEmpty()) {
                addError(errors, "第 " + (i + 1) + " 行：品牌「" + brandName + "」不存在，请先在品牌管理中创建");
                continue;
            }
            Product product = new Product();
            product.setName(name);
            product.setCategoryId(categoryId);
            product.setBrandId(brandId);
            product.setPrice(parseDecimal(BrandService.cell(row, 3)));
            product.setMarketPrice(parseDecimal(BrandService.cell(row, 4)));
            product.setStock(BrandService.parseInt(BrandService.cell(row, 5), 0));
            product.setSales(BrandService.parseInt(BrandService.cell(row, 6), 0));
            product.setImage(BrandService.cell(row, 7).isEmpty() ? null : BrandService.cell(row, 7));
            String status = BrandService.cell(row, 8);
            product.setStatus("下架".equals(status) || "隐藏".equals(status) ? 0 : BrandService.parseInt(status, 1) == 0 ? 0 : 1);
            product.setDescription(BrandService.cell(row, 9).isEmpty() ? null : BrandService.cell(row, 9));
            product.setCreatedBy(CurrentAdmin.username());
            productRepository.save(product);
            success++;
        }
        return new ImportResult(success, errors.size(), errors);
    }

    private void validateReferences(ProductRequest request) {
        if (request.categoryId() != null && !categoryRepository.existsById(request.categoryId())) {
            throw new BusinessException("所选分类不存在，请刷新后重试");
        }
        if (request.brandId() != null && !brandRepository.existsById(request.brandId())) {
            throw new BusinessException("所选品牌不存在，请刷新后重试");
        }
    }

    private void apply(Product product, ProductRequest request) {
        product.setName(request.name().trim());
        product.setDescription(blankToNull(request.description()));
        product.setCategoryId(request.categoryId());
        product.setBrandId(request.brandId());
        product.setPrice(request.price() == null ? BigDecimal.ZERO : request.price());
        product.setMarketPrice(request.marketPrice() == null ? BigDecimal.ZERO : request.marketPrice());
        product.setStock(request.stock() == null ? 0 : request.stock());
        product.setSales(request.sales() == null ? 0 : request.sales());
        product.setImage(blankToNull(request.image()));
        product.setStatus(request.status() == null ? 1 : request.status());
    }

    private ProductView toView(Product product, Map<Long, String> categoryNames, Map<Long, String> brandNames) {
        String categoryName = null;
        String brandName = null;
        if (categoryNames != null && product.getCategoryId() != null) {
            categoryName = categoryNames.get(product.getCategoryId());
        }
        if (brandNames != null && product.getBrandId() != null) {
            brandName = brandNames.get(product.getBrandId());
        }
        return new ProductView(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategoryId(),
                categoryName,
                product.getBrandId(),
                brandName,
                product.getPrice(),
                product.getMarketPrice(),
                product.getStock(),
                product.getSales(),
                product.getImage(),
                product.getStatus(),
                product.getCreatedBy(),
                product.getCreatedAt()
        );
    }

    private static BigDecimal parseDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (RuntimeException e) {
            return BigDecimal.ZERO;
        }
    }

    private static void addError(List<String> errors, String message) {
        if (errors.size() < MAX_ERRORS) {
            errors.add(message);
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
