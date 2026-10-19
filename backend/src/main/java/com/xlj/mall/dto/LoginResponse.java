package com.xlj.mall.dto;

public record LoginResponse(String token, String adminName, String nickname, String role) {
}
