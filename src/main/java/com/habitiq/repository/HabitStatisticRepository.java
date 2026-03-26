package com.habitiq.repository;

import com.habitiq.entity.HabitStatistic;
import com.habitiq.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitStatisticRepository extends JpaRepository<HabitStatistic, Long> {
    Optional<HabitStatistic> findByHabitAndDate(Habit habit, LocalDate date);
    List<HabitStatistic> findByHabitAndDateBetween(Habit habit, LocalDate startDate, LocalDate endDate);
    List<HabitStatistic> findByHabit(Habit habit);
}
