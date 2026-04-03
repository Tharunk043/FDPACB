package com.FDPACB.FDPACB.service;

import com.FDPACB.FDPACB.dto.dashboard.DashboardSummary;
import com.FDPACB.FDPACB.dto.dashboard.MonthlyTrendEntry;
import com.FDPACB.FDPACB.dto.dashboard.RecentActivityEntry;
import com.FDPACB.FDPACB.entity.FinancialRecord;
import com.FDPACB.FDPACB.enums.RecordType;
import com.FDPACB.FDPACB.repository.FinancialRecordRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

@Service
public class DashboardService {

    private final FinancialRecordRepository recordRepository;

    public DashboardService(FinancialRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummary getSummary() {
        BigDecimal totalIncome  = recordRepository.sumByType(RecordType.INCOME);
        BigDecimal totalExpense = recordRepository.sumByType(RecordType.EXPENSE);
        BigDecimal netBalance   = totalIncome.subtract(totalExpense);

        Map<String, BigDecimal> categoryTotals = buildCategoryTotals();
        List<MonthlyTrendEntry> monthlyTrend   = buildMonthlyTrend();
        List<RecentActivityEntry> recent       = buildRecentActivity();

        return new DashboardSummary(totalIncome, totalExpense, netBalance, categoryTotals, monthlyTrend, recent);
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getCategoryTotals() {
        return buildCategoryTotals();
    }

    @Transactional(readOnly = true)
    public List<MonthlyTrendEntry> getMonthlyTrend() {
        return buildMonthlyTrend();
    }

    @Transactional(readOnly = true)
    public List<RecentActivityEntry> getRecentActivity() {
        return buildRecentActivity();
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private Map<String, BigDecimal> buildCategoryTotals() {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Object[] row : recordRepository.getCategoryTotals()) {
            result.put(row[0].toString(), (BigDecimal) row[1]);
        }
        return result;
    }

    private List<MonthlyTrendEntry> buildMonthlyTrend() {
        // Get last 6 months
        LocalDate since = LocalDate.now().minusMonths(5).withDayOfMonth(1);
        List<Object[]> rows = recordRepository.getMonthlyTrend(since);

        // Aggregate into a map keyed by "year-month"
        Map<String, BigDecimal[]> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            int year      = ((Number) row[0]).intValue();
            int month     = ((Number) row[1]).intValue();
            RecordType t  = RecordType.valueOf(row[2].toString());
            BigDecimal amt = (BigDecimal) row[3];

            String key = year + "-" + month;
            map.computeIfAbsent(key, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, year * 100 + month > 0 ? BigDecimal.ZERO : BigDecimal.ZERO});

            BigDecimal[] arr = map.get(key);
            if (t == RecordType.INCOME) {
                arr[0] = arr[0].add(amt);
            } else {
                arr[1] = arr[1].add(amt);
            }
            // store year/month as hack in arr[2] (we re-parse the key anyway)
        }

        List<MonthlyTrendEntry> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal[]> e : map.entrySet()) {
            String[] parts = e.getKey().split("-");
            int year  = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            BigDecimal income  = e.getValue()[0];
            BigDecimal expense = e.getValue()[1];
            String monthName = Month.of(month).name();
            result.add(new MonthlyTrendEntry(year, month, monthName, income, expense, income.subtract(expense)));
        }

        // Sort by year desc then month desc
        result.sort(Comparator.comparingInt(MonthlyTrendEntry::year)
                .thenComparingInt(MonthlyTrendEntry::month).reversed());
        return result;
    }

    private List<RecentActivityEntry> buildRecentActivity() {
        List<FinancialRecord> records = recordRepository.findRecentActivity(PageRequest.of(0, 10));
        return records.stream().map(r -> new RecentActivityEntry(
                r.getId(),
                r.getAmount(),
                r.getType(),
                r.getCategory(),
                r.getDate(),
                r.getNotes(),
                r.getCreatedBy().getUsername(),
                r.getCreatedAt()
        )).toList();
    }
}
