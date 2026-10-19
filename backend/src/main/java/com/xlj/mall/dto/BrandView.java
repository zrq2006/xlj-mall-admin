package com.xlj.mall.dto;

import java.time.LocalDateTime;

public record BrandView(
        Long id,
        String name,
        String logo,
        String firstLetter,
        String description,
        Integer sort,
        Integer status,
        String createdBy,
        LocalDateTime createdAt,
        boolean editable
) {
}
