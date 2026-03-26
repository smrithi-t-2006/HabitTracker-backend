package com.habitiq.repository;

import com.habitiq.entity.DailySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailySummaryRepository extends JpaRepository<DailySummary, Long> {
    Optional<DailySummary> findByUserIdAndSummaryDate(Long userId, LocalDate summaryDate);
    List<DailySummary> findByUserIdAndSummaryDateBetweenOrderBySummaryDateAsc(Long userId, LocalDate start, LocalDate end);
}
