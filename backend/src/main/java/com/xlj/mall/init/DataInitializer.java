package com.xlj.mall.init;

import com.xlj.mall.entity.AdminUser;
import com.xlj.mall.repository.AdminUserRepository;
import com.xlj.mall.security.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 首次启动时创建默认管理员账号（admin / admin123），已存在则跳过。
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordHasher passwordHasher;
    private final String username;
    private final String password;
    private final String nickname;

    public DataInitializer(AdminUserRepository adminUserRepository,
                           PasswordHasher passwordHasher,
                           @Value("${app.auth.default-username}") String username,
                           @Value("${app.auth.default-password}") String password,
                           @Value("${app.auth.default-nickname}") String nickname) {
        this.adminUserRepository = adminUserRepository;
        this.passwordHasher = passwordHasher;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminUserRepository.existsByUsername(username)) {
            return;
        }
        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        admin.setPassword(passwordHasher.encode(password));
        admin.setNickname(nickname);
        admin.setRole("admin");
        admin.setStatus(1);
        admin.setCreatedAt(LocalDateTime.now().withNano(0));
        adminUserRepository.save(admin);
        log.info("已创建默认管理员账号：{} / {}（请及时修改密码）", username, password);
    }
}

