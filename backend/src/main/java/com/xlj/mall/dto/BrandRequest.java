package com.xlj.mall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BrandRequest(
        @NotBlank(message = "请输入品牌名称")
        @Size(max = 100, message = "品牌名称不能超过 100 个字符")
        String name,

        @Size(max = 500, message = "Logo 地址过长")
        String logo,

        @Size(max = 1, message = "首字母只能填写 1 个字符")
        String firstLetter,

        @Size(max = 500, message = "品牌描述不能超过 500 个字符")
        String description,

        @Min(value = 0, message = "排序不能小于 0")
        Integer sort,

        Integer status
) {
}
