package com.xlj.mall.controller;

import com.xlj.mall.common.ApiResponse;
import com.xlj.mall.dto.DashboardStats;
import com.xlj.mall.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "02. 工作台")
@RestController
@RequestMapping("/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "经营概览指标（实时统计）")
    @GetMapping("/stats")
    public ApiResponse<DashboardStats> stats() {
        return ApiResponse.ok(dashboardService.stats());
    }
}
