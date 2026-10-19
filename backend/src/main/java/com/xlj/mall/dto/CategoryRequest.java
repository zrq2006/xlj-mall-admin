package com.xlj.mall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "请输入分类名称")
        @Size(max = 50, message = "分类名称不能超过 50 个字符")
        String name,

        @Size(max = 200, message = "分类图标不能超过 200 个字符")
        String icon,

        @Size(max = 500, message = "分类图片地址过长")
        String image,

        @Min(value = 0, message = "排序不能小于 0")
        Integer sort,

        Integer status
) {
}
