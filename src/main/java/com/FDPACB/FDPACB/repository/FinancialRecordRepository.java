package com.FDPACB.FDPACB.repository;

import com.FDPACB.FDPACB.entity.FinancialRecord;
import com.FDPACB.FDPACB.enums.Category;
import com.FDPACB.FDPACB.enums.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, UUID> {

    Optional<FinancialRecord> findByIdAndDeletedFalse(UUID id);

    @Query("""
            SELECT r FROM FinancialRecord r
            WHERE r.deleted = false
              AND (:type IS NULL OR r.type = :type)
              AND (:category IS NULL OR r.category = :category)
              AND (:from IS NULL OR r.date >= :from)
              AND (:to IS NULL OR r.date <= :to)
              AND (:search IS NULL OR LOWER(r.notes) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY r.date DESC, r.createdAt DESC
            """)
    Page<FinancialRecord> findFiltered(
            @Param("type") RecordType type,
            @Param("category") Category category,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("search") String search,
            Pageable pageable
    );

    // ─── Dashboard Aggregations ───────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.deleted = false AND r.type = :type")
    BigDecimal sumByType(@Param("type") RecordType type);

    @Query("""
            SELECT r.category, COALESCE(SUM(r.amount), 0)
            FROM FinancialRecord r
            WHERE r.deleted = false
            GROUP BY r.category
            ORDER BY SUM(r.amount) DESC
            """)
    List<Object[]> getCategoryTotals();

    @Query("""
            SELECT FUNCTION('YEAR', r.date), FUNCTION('MONTH', r.date), r.type, COALESCE(SUM(r.amount), 0)
            FROM FinancialRecord r
            WHERE r.deleted = false AND r.date >= :since
            GROUP BY FUNCTION('YEAR', r.date), FUNCTION('MONTH', r.date), r.type
            ORDER BY FUNCTION('YEAR', r.date) DESC, FUNCTION('MONTH', r.date) DESC
            """)
    List<Object[]> getMonthlyTrend(@Param("since") LocalDate since);

    @Query("""
            SELECT r FROM FinancialRecord r
            WHERE r.deleted = false
            ORDER BY r.createdAt DESC
            """)
    List<FinancialRecord> findRecentActivity(Pageable pageable);
}
