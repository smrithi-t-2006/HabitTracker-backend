package com.habitiq.repository;

import com.habitiq.entity.HabitTemplate;
import com.habitiq.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitTemplateRepository extends JpaRepository<HabitTemplate, Long> {
    Optional<HabitTemplate> findByNameIgnoreCase(String name);
    List<HabitTemplate> findByCategory(Category category);
    List<HabitTemplate> findByIsPopularTrue();
    
    @Query("SELECT h FROM HabitTemplate h ORDER BY h.usageCount DESC")
    List<HabitTemplate> findMostUsedTemplates();
}
