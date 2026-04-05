package com.FDPACB.FDPACB.service;

import com.FDPACB.FDPACB.dto.record.CreateRecordRequest;
import com.FDPACB.FDPACB.dto.record.RecordResponse;
import com.FDPACB.FDPACB.dto.record.UpdateRecordRequest;
import com.FDPACB.FDPACB.entity.FinancialRecord;
import com.FDPACB.FDPACB.entity.User;
import com.FDPACB.FDPACB.enums.Category;
import com.FDPACB.FDPACB.enums.RecordType;
import com.FDPACB.FDPACB.exception.ResourceNotFoundException;
import com.FDPACB.FDPACB.repository.FinancialRecordRepository;
import com.FDPACB.FDPACB.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;

    public FinancialRecordService(FinancialRecordRepository recordRepository,
                                   UserRepository userRepository) {
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RecordResponse createRecord(CreateRecordRequest req) {
        User currentUser = getCurrentUser();

        FinancialRecord record = new FinancialRecord();
        record.setAmount(req.amount());
        record.setType(req.type());
        record.setCategory(req.category());
        record.setDate(req.date());
        record.setNotes(req.notes());
        record.setCreatedBy(currentUser);

        return RecordResponse.from(recordRepository.save(record));
    }

    public Page<RecordResponse> getRecords(RecordType type,
                                            Category category,
                                            LocalDate from,
                                            LocalDate to,
                                            String search,
                                            int page,
                                            int size) {
        Pageable pageable = PageRequest.of(page, size);
        return recordRepository.findFiltered(type, category, from, to, search, pageable)
                .map(RecordResponse::from);
    }

    public RecordResponse getRecordById(UUID id) {
        FinancialRecord record = recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialRecord", id));
        return RecordResponse.from(record);
    }

    @Transactional
    public RecordResponse updateRecord(UUID id, UpdateRecordRequest req) {
        FinancialRecord record = recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialRecord", id));

        if (req.amount() != null)   record.setAmount(req.amount());
        if (req.type() != null)     record.setType(req.type());
        if (req.category() != null) record.setCategory(req.category());
        if (req.date() != null)     record.setDate(req.date());
        if (req.notes() != null)    record.setNotes(req.notes());

        return RecordResponse.from(recordRepository.save(record));
    }

    @Transactional
    public void deleteRecord(UUID id) {
        FinancialRecord record = recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialRecord", id));
        record.setDeleted(true);
        recordRepository.save(record);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found: " + username));
    }
}
