package com.FDPACB.FDPACB.controller;

import com.FDPACB.FDPACB.dto.dashboard.DashboardSummary;
import com.FDPACB.FDPACB.dto.dashboard.MonthlyTrendEntry;
import com.FDPACB.FDPACB.dto.dashboard.RecentActivityEntry;
import com.FDPACB.FDPACB.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Analytics and summary endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    @Operation(summary = "Full dashboard summary [ALL ROLES]",
               description = "Returns total income, expense, net balance, category totals, monthly trend, and recent activity.")
    public ResponseEntity<DashboardSummary> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/category-totals")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Operation(summary = "Category-wise totals [ANALYST, ADMIN]")
    public ResponseEntity<Map<String, BigDecimal>> getCategoryTotals() {
        return ResponseEntity.ok(dashboardService.getCategoryTotals());
    }

    @GetMapping("/monthly-trends")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Operation(summary = "Monthly income/expense trends (last 6 months) [ANALYST, ADMIN]")
    public ResponseEntity<List<MonthlyTrendEntry>> getMonthlyTrends() {
        return ResponseEntity.ok(dashboardService.getMonthlyTrend());
    }

    @GetMapping("/recent-activity")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    @Operation(summary = "Recent 10 activities [ALL ROLES]")
    public ResponseEntity<List<RecentActivityEntry>> getRecentActivity() {
        return ResponseEntity.ok(dashboardService.getRecentActivity());
    }
}
