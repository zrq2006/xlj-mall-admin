package com.xlj.mall.service;

import com.xlj.mall.common.BusinessException;
import com.xlj.mall.common.PageResult;
import com.xlj.mall.dto.CategoryRequest;
import com.xlj.mall.dto.CategoryView;
import com.xlj.mall.entity.Category;
import com.xlj.mall.repository.CategoryRepository;
import com.xlj.mall.repository.ProductRepository;
import com.xlj.mall.security.CurrentAdmin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<CategoryView> page(String name, Integer status, int page, int size) {
        Page<Category> result = categoryRepository.search(blankToNull(name), status,
                PageRequest.of(page - 1, size));
        return new PageResult<>(result.getContent().stream().map(this::toView).toList(),
                result.getTotalElements(), page, size);
    }

    @Transactional(readOnly = true)
    public List<CategoryView> listAll() {
        return categoryRepository.findAllByOrderBySortAscIdAsc().stream().map(this::toView).toList();
    }

    @Transactional
    public CategoryView create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name().trim())) {
            throw new BusinessException("分类名称已存在");
        }
        Category category = new Category();
        apply(category, request);
        category.setCreatedBy(CurrentAdmin.username());
        return toView(categoryRepository.save(category));
    }

    @Transactional
    public CategoryView update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "分类不存在或已被删除"));
        if (categoryRepository.existsByNameAndIdNot(request.name().trim(), id)) {
            throw new BusinessException("分类名称已存在");
        }
        apply(category, request);
        return toView(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "分类不存在或已被删除"));
        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new BusinessException("该分类下还有 " + productCount + " 个商品，请先调整商品分类再删除");
        }
        categoryRepository.delete(category);
    }

    @Transactional
    public CategoryView updateStatus(Long id, Integer status) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "分类不存在或已被删除"));
        category.setStatus(status);
        return toView(categoryRepository.save(category));
    }

    private void apply(Category category, CategoryRequest request) {
        category.setName(request.name().trim());
        category.setIcon(blankToNull(request.icon()));
        category.setImage(blankToNull(request.image()));
        category.setSort(request.sort() == null ? 0 : request.sort());
        category.setStatus(request.status() == null ? 1 : request.status());
    }

    private CategoryView toView(Category category) {
        return new CategoryView(
                category.getId(),
                category.getName(),
                category.getIcon(),
                category.getImage(),
                category.getSort(),
                category.getStatus(),
                category.getCreatedBy(),
                category.getCreatedAt(),
                productRepository.countByCategoryId(category.getId())
        );
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
