package com.xlj.mall.service;

import com.xlj.mall.common.BusinessException;
import com.xlj.mall.common.PageResult;
import com.xlj.mall.dto.BrandRequest;
import com.xlj.mall.dto.BrandView;
import com.xlj.mall.dto.ImportResult;
import com.xlj.mall.entity.Brand;
import com.xlj.mall.repository.BrandRepository;
import com.xlj.mall.repository.ProductRepository;
import com.xlj.mall.security.CurrentAdmin;
import com.xlj.mall.support.CsvUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class BrandService {

    private static final int MAX_ERRORS = 10;

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    public BrandService(BrandRepository brandRepository, ProductRepository productRepository) {
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<BrandView> page(String name, Integer status, int page, int size) {
        Page<Brand> result = brandRepository.search(blankToNull(name), status, PageRequest.of(page - 1, size));
        return new PageResult<>(result.getContent().stream().map(this::toView).toList(),
                result.getTotalElements(), page, size);
    }

    @Transactional(readOnly = true)
    public List<BrandView> listAll() {
        return brandRepository.findAllByOrderBySortAscIdAsc().stream().map(this::toView).toList();
    }

    @Transactional
    public BrandView create(BrandRequest request) {
        String name = request.name().trim();
        if (brandRepository.existsByName(name)) {
            throw new BusinessException("品牌名称已存在");
        }
        Brand brand = new Brand();
        apply(brand, request);
        brand.setCreatedBy(CurrentAdmin.username());
        return toView(brandRepository.save(brand));
    }

    @Transactional
    public BrandView update(Long id, BrandRequest request) {
        Brand brand = requireOwnedBrand(id);
        String name = request.name().trim();
        if (brandRepository.existsByNameAndIdNot(name, id)) {
            throw new BusinessException("品牌名称已存在");
        }
        apply(brand, request);
        return toView(brandRepository.save(brand));
    }

    @Transactional
    public void delete(Long id) {
        Brand brand = requireOwnedBrand(id);
        long productCount = productRepository.countByBrandId(id);
        if (productCount > 0) {
            throw new BusinessException("该品牌下还有 " + productCount + " 个商品，请先调整商品品牌再删除");
        }
        brandRepository.delete(brand);
    }

    @Transactional
    public BrandView updateStatus(Long id, Integer status) {
        Brand brand = requireOwnedBrand(id);
        brand.setStatus(status);
        return toView(brandRepository.save(brand));
    }

    /**
     * 下载批量导入模板（CSV，带 UTF-8 BOM，Excel 可直接打开）。
     */
    public byte[] templateCsv() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"品牌名称", "首字母", "品牌描述", "排序", "状态", "Logo"});
        rows.add(new String[]{"示例品牌", "S", "这里填写品牌描述", "100", "显示", ""});
        String content = "\uFEFF" + CsvUtil.build(rows);
        return content.getBytes(StandardCharsets.UTF_8);
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
        List<String> errors = new ArrayList<>();
        int success = 0;
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            String name = cell(row, 0);
            if (name.isEmpty()) {
                addError(errors, "第 " + (i + 1) + " 行：品牌名称不能为空");
                continue;
            }
            if (brandRepository.existsByName(name)) {
                addError(errors, "第 " + (i + 1) + " 行：" + name + " 已存在");
                continue;
            }
            Brand brand = new Brand();
            brand.setName(name);
            brand.setFirstLetter(cell(row, 1).isEmpty() ? defaultLetter(name) : cell(row, 1).toUpperCase(Locale.ROOT));
            brand.setDescription(cell(row, 2).isEmpty() ? null : cell(row, 2));
            brand.setSort(parseInt(cell(row, 3), 0));
            brand.setStatus("隐藏".equals(cell(row, 4)) ? 0 : parseInt(cell(row, 4), 1) == 0 ? 0 : 1);
            brand.setLogo(cell(row, 5).isEmpty() ? null : cell(row, 5));
            brand.setCreatedBy(CurrentAdmin.username());
            brandRepository.save(brand);
            success++;
        }
        return new ImportResult(success, errors.size(), errors);
    }

    private Brand requireOwnedBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "品牌不存在或已被删除"));
        if (!brand.getCreatedBy().equals(CurrentAdmin.username())) {
            throw new BusinessException(403, "仅创建者可操作该品牌");
        }
        return brand;
    }

    private void apply(Brand brand, BrandRequest request) {
        brand.setName(request.name().trim());
        brand.setLogo(blankToNull(request.logo()));
        String letter = request.firstLetter() == null ? "" : request.firstLetter().trim();
        brand.setFirstLetter(letter.isEmpty() ? defaultLetter(request.name().trim()) : letter.toUpperCase(Locale.ROOT));
        brand.setDescription(blankToNull(request.description()));
        brand.setSort(request.sort() == null ? 0 : request.sort());
        brand.setStatus(request.status() == null ? 1 : request.status());
    }

    private BrandView toView(Brand brand) {
        return new BrandView(
                brand.getId(),
                brand.getName(),
                brand.getLogo(),
                brand.getFirstLetter(),
                brand.getDescription(),
                brand.getSort(),
                brand.getStatus(),
                brand.getCreatedBy(),
                brand.getCreatedAt(),
                CurrentAdmin.username().equals(brand.getCreatedBy())
        );
    }

    static String defaultLetter(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        char first = name.charAt(0);
        return Character.isLetter(first) ? String.valueOf(first).toUpperCase(Locale.ROOT) : String.valueOf(first);
    }

    static String cell(String[] row, int index) {
        return index < row.length && row[index] != null ? row[index].trim() : "";
    }

    static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
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
