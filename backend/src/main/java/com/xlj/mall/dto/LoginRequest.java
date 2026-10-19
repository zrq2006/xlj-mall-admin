package com.xlj.mall.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "请输入登录账号") String username,
        @NotBlank(message = "请输入登录密码") String password
) {
}
