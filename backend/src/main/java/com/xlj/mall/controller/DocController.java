package com.xlj.mall.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

/**
 * 工作台「接口文档」链接指向 /doc.html，这里重定向到 OpenAPI 页面。
 */
@Hidden
@Controller
public class DocController {

    @GetMapping("/doc.html")
    public RedirectView doc() {
        return new RedirectView("/swagger-ui/index.html");
    }
}
