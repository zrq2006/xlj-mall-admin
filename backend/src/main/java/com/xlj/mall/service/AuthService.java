package com.xlj.mall.service;

import com.xlj.mall.common.BusinessException;
import com.xlj.mall.dto.LoginRequest;
import com.xlj.mall.dto.LoginResponse;
import com.xlj.mall.dto.MeResponse;
import com.xlj.mall.entity.AdminUser;
import com.xlj.mall.repository.AdminUserRepository;
import com.xlj.mall.security.CurrentAdmin;
import com.xlj.mall.security.PasswordHasher;
import com.xlj.mall.security.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;

    public AuthService(AdminUserRepository adminUserRepository,
                       PasswordHasher passwordHasher,
                       TokenService tokenService) {
        this.adminUserRepository = adminUserRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        AdminUser user = adminUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(401, "账号或密码错误"));
        if (!passwordHasher.matches(request.password(), user.getPassword())) {
            throw new BusinessException(401, "账号或密码错误");
        }
        if (!Objects.equals(user.getStatus(), 1)) {
            throw new BusinessException(403, "账号已被禁用，请联系系统管理员");
        }
        String token = tokenService.create(user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getUsername(), user.getNickname(), user.getRole());
    }

    @Transactional(readOnly = true)
    public MeResponse me() {
        String username = CurrentAdmin.username();
        AdminUser user = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(401, "登录状态已失效，请重新登录"));
        return new MeResponse(user.getUsername(), user.getNickname(), user.getRole());
    }
}
