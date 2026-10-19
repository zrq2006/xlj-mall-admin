package com.xlj.mall.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI 配置：支持直接填入 Bearer 令牌调试受保护接口。
 */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "bearerAuth";

    @Bean
    public OpenAPI adminOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("小蓝鲸优选 · 管理后台 API")
                        .version("1.0.0")
                        .description("工作台、商品列表、分类管理、品牌管理接口。登录后把返回的 token 填入 Authorize 即可调试。"))
                .components(new Components().addSecuritySchemes(SCHEME,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("token")))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME));
    }
}
