package com.FDPACB.FDPACB.dto.dashboard;

import java.math.BigDecimal;

public record MonthlyTrendEntry(
        int year,
        int month,
        String monthName,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net
) {}
