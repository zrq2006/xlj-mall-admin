package com.xlj.mall.config;

import com.xlj.mall.security.AuthInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/auth/login");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    /**
     * 前端使用 history 路由：静态资源命中即返回，目录与未命中的页面路径回退到 index.html。
     *
     * <p>前端产物可能位于三处，按顺序查找：
     * <ol>
     *   <li>{@code classpath:/static/}：Maven 打包时嵌入 jar 的产物；</li>
     *   <li>{@code <project>/frontend/dist/}：在项目根目录用 java -jar 运行；</li>
     *   <li>{@code <project>/frontend/dist/}（相对 backend 模块的上一级）：IDEA 直接运行 Application。</li>
     * </ol>
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        List<String> locations = new ArrayList<>();
        locations.add("classpath:/static/");
        String userDir = System.getProperty("user.dir");
        for (Path candidate : new Path[]{
                Paths.get(userDir, "frontend", "dist"),
                Paths.get(userDir, "..", "frontend", "dist").normalize()}) {
            Path normalized = candidate.toAbsolutePath().normalize();
            if (Files.isDirectory(normalized)) {
                locations.add(normalized.toUri().toString());
                log.info("前端静态资源目录：{}", normalized);
            }
        }

        registry.addResourceHandler("/**")
                .addResourceLocations(locations.toArray(String[]::new))
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        if (resourcePath.startsWith("admin/")
                                || resourcePath.startsWith("swagger-ui")
                                || resourcePath.startsWith("webjars")
                                || resourcePath.startsWith("v3/api-docs")) {
                            return null;
                        }
                        // 空路径（如 "/"）与目录不直接返回，交给 index.html 处理
                        String lastSegment = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
                        boolean looksLikeAsset = lastSegment.contains(".");
                        if (!resourcePath.isEmpty() && !resourcePath.endsWith("/")) {
                            Resource requested = location.createRelative(resourcePath);
                            if (requested.exists() && requested.isReadable() && !isDirectory(requested)) {
                                return requested;
                            }
                        }
                        // 只有“像页面”的路径（不含扩展名）才回退到 index.html，缺失的 js/css 直接 404
                        if (looksLikeAsset) {
                            return null;
                        }
                        Resource index = location.createRelative("index.html");
                        return index.exists() && !isDirectory(index) ? index : null;
                    }
                });
    }

    /**
     * 判断资源是否为目录：文件系统资源可以取到 File，jar 内资源无法判断，按文件处理。
     */
    private static boolean isDirectory(Resource resource) {
        try {
            if (resource instanceof FileUrlResource fileUrlResource) {
                return fileUrlResource.getFile().isDirectory();
            }
            if (resource instanceof FileSystemResource fileSystemResource) {
                return fileSystemResource.getFile().isDirectory();
            }
            if (resource instanceof ClassPathResource classPathResource) {
                return classPathResource.getFile().isDirectory();
            }
        } catch (IOException | RuntimeException ignored) {
            // 打包在 jar 内的 classpath 资源没有对应的 File，忽略即可
        }
        return false;
    }
}
