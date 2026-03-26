package com.habitiq.repository;

import com.habitiq.entity.HabitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {

    Optional<HabitLog> findByHabitIdAndLogDate(Long habitId, LocalDate logDate);

    List<HabitLog> findByHabitIdAndLogDateBetween(Long habitId, LocalDate start, LocalDate end);

    List<HabitLog> findByHabitIdOrderByLogDateDesc(Long habitId);

    @Query("SELECT hl FROM HabitLog hl WHERE hl.habit.user.id = :userId AND hl.logDate = :date")
    List<HabitLog> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT hl FROM HabitLog hl WHERE hl.habit.user.id = :userId AND hl.logDate BETWEEN :start AND :end")
    List<HabitLog> findByUserIdAndDateRange(@Param("userId") Long userId,
                                             @Param("start") LocalDate start,
                                             @Param("end") LocalDate end);

    @Query("SELECT COUNT(hl) FROM HabitLog hl WHERE hl.habit.id = :habitId AND hl.completed = true AND hl.logDate BETWEEN :start AND :end")
    Long countCompletedByHabitAndDateRange(@Param("habitId") Long habitId,
                                           @Param("start") LocalDate start,
                                           @Param("end") LocalDate end);

    @Query("SELECT COUNT(hl) FROM HabitLog hl WHERE hl.habit.id = :habitId AND hl.logDate BETWEEN :start AND :end")
    Long countByHabitAndDateRange(@Param("habitId") Long habitId,
                                  @Param("start") LocalDate start,
                                  @Param("end") LocalDate end);
}
