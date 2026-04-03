package com.FDPACB.FDPACB.dto.dashboard;

import com.FDPACB.FDPACB.enums.Category;
import com.FDPACB.FDPACB.enums.RecordType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecentActivityEntry(
        UUID id,
        BigDecimal amount,
        RecordType type,
        Category category,
        LocalDate date,
        String notes,
        String createdBy,
        LocalDateTime createdAt
) {}
