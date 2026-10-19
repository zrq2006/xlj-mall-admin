package com.xlj.mall.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductView(
        Long id,
        String name,
        String description,
        Long categoryId,
        String categoryName,
        Long brandId,
        String brandName,
        BigDecimal price,
        BigDecimal marketPrice,
        Integer stock,
        Integer sales,
        String image,
        Integer status,
        String createdBy,
        LocalDateTime createdAt
) {
}
