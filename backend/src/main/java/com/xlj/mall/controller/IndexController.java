package com.xlj.mall.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 站点根路径统一转发到前端入口页。
 *
 * <p>Spring 的资源解析链不会为空路径（{@code "/"}）调用自定义解析器，因此这里显式转发，
 * 保证 IDEA 直接运行（没有 classpath:/static/index.html）时首页也能打开。
 */
@Hidden
@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}
