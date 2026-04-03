package com.FDPACB.FDPACB.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardSummary(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance,
        Map<String, BigDecimal> categoryTotals,
        List<MonthlyTrendEntry> monthlyTrend,
        List<RecentActivityEntry> recentActivity
) {}
