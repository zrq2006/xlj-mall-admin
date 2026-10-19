package com.xlj.mall.dto;

import java.time.LocalDateTime;

public record CategoryView(
        Long id,
        String name,
        String icon,
        String image,
        Integer sort,
        Integer status,
        String createdBy,
        LocalDateTime createdAt,
        long productCount
) {
}
