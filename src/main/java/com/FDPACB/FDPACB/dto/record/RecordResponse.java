package com.FDPACB.FDPACB.dto.record;

import com.FDPACB.FDPACB.entity.FinancialRecord;
import com.FDPACB.FDPACB.enums.Category;
import com.FDPACB.FDPACB.enums.RecordType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecordResponse(
        UUID id,
        BigDecimal amount,
        RecordType type,
        Category category,
        LocalDate date,
        String notes,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RecordResponse from(FinancialRecord r) {
        return new RecordResponse(
                r.getId(),
                r.getAmount(),
                r.getType(),
                r.getCategory(),
                r.getDate(),
                r.getNotes(),
                r.getCreatedBy().getUsername(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
