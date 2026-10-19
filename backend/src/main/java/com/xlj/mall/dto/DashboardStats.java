package com.xlj.mall.dto;

import java.math.BigDecimal;

/**
 * 工作台「经营概览」指标。
 */
public record DashboardStats(
        long todayOrderCount,
        BigDecimal todayPayAmount,
        long pendingPay,
        long pendingShip,
        long totalOrderCount,
        long totalUserCount,
        long merchantPending
) {
}
