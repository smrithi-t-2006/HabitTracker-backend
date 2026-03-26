package com.habitiq.repository;

import com.habitiq.entity.Insight;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InsightRepository extends JpaRepository<Insight, Long> {
    List<Insight> findByUserIdOrderByGeneratedAtDesc(Long userId);
    List<Insight> findByUserIdAndIsReadFalseOrderByGeneratedAtDesc(Long userId);
    List<Insight> findByUserIdAndTypeOrderByGeneratedAtDesc(Long userId, Insight.InsightType type);
    Long countByUserIdAndIsReadFalse(Long userId);
}
