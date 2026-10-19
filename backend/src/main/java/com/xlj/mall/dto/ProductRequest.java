package com.xlj.mall.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "请输入商品名称")
        @Size(max = 200, message = "商品名称不能超过 200 个字符")
        String name,

        @Size(max = 1000, message = "商品描述不能超过 1000 个字符")
        String description,

        @NotNull(message = "请选择商品分类")
        Long categoryId,

        Long brandId,

        @NotNull(message = "请输入售价")
        @DecimalMin(value = "0", message = "售价不能小于 0")
        BigDecimal price,

        @DecimalMin(value = "0", message = "同行价不能小于 0")
        BigDecimal marketPrice,

        @NotNull(message = "请输入库存")
        @Min(value = 0, message = "库存不能小于 0")
        Integer stock,

        @Min(value = 0, message = "销量不能小于 0")
        Integer sales,

        String image,

        Integer status
) {
}
