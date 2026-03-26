package com.habitiq.repository;

import com.habitiq.entity.Habit;
import com.habitiq.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HabitRepository extends JpaRepository<Habit, Long> {
    List<Habit> findByUserIdAndIsActiveTrue(Long userId);
    List<Habit> findByUserId(Long userId);
    List<Habit> findByUserIdAndCategory(Long userId, Category category);
}
