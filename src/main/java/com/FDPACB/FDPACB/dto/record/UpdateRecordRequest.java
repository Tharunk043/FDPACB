package com.FDPACB.FDPACB.dto.record;

import com.FDPACB.FDPACB.enums.Category;
import com.FDPACB.FDPACB.enums.RecordType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateRecordRequest(

        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @Digits(integer = 13, fraction = 2, message = "Amount format invalid")
        BigDecimal amount,

        RecordType type,

        Category category,

        LocalDate date,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {}
