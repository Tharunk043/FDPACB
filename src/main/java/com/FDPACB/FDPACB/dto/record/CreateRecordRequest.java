package com.FDPACB.FDPACB.dto.record;

import com.FDPACB.FDPACB.enums.Category;
import com.FDPACB.FDPACB.enums.RecordType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateRecordRequest(

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @Digits(integer = 13, fraction = 2, message = "Amount format invalid")
        BigDecimal amount,

        @NotNull(message = "Type is required (INCOME or EXPENSE)")
        RecordType type,

        @NotNull(message = "Category is required")
        Category category,

        @NotNull(message = "Date is required")
        LocalDate date,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {}
