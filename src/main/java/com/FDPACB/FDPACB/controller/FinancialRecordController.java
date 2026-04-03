package com.FDPACB.FDPACB.controller;

import com.FDPACB.FDPACB.dto.record.CreateRecordRequest;
import com.FDPACB.FDPACB.dto.record.RecordResponse;
import com.FDPACB.FDPACB.dto.record.UpdateRecordRequest;
import com.FDPACB.FDPACB.enums.Category;
import com.FDPACB.FDPACB.enums.RecordType;
import com.FDPACB.FDPACB.service.FinancialRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/records")
@Tag(name = "Financial Records", description = "CRUD and filtering for financial transactions")
@SecurityRequirement(name = "bearerAuth")
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    public FinancialRecordController(FinancialRecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Operation(summary = "Create a financial record [ANALYST, ADMIN]")
    public ResponseEntity<RecordResponse> create(@Valid @RequestBody CreateRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recordService.createRecord(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    @Operation(summary = "List records with optional filters [ALL ROLES]",
               description = "Filter by type, category, date range. Supports pagination.")
    public ResponseEntity<Page<RecordResponse>> getAll(
            @RequestParam(required = false) RecordType type,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(recordService.getRecords(type, category, from, to, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    @Operation(summary = "Get a single record by ID [ALL ROLES]")
    public ResponseEntity<RecordResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(recordService.getRecordById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a financial record [ADMIN]")
    public ResponseEntity<RecordResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody UpdateRecordRequest request) {
        return ResponseEntity.ok(recordService.updateRecord(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a financial record [ADMIN]")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        recordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }
}
