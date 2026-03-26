package com.habitiq.repository;

import com.habitiq.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    Optional<Reminder> findByHabitId(Long habitId);

    @Query("SELECT r FROM Reminder r WHERE r.isEnabled = true AND r.reminderTime BETWEEN :start AND :end")
    List<Reminder> findDueReminders(@Param("start") LocalTime start, @Param("end") LocalTime end);

    @Query("SELECT r FROM Reminder r WHERE r.habit.user.id = :userId")
    List<Reminder> findByUserId(@Param("userId") Long userId);
}
