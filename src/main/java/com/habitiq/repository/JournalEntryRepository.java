package com.habitiq.repository;

import com.habitiq.entity.JournalEntry;
import com.habitiq.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    List<JournalEntry> findByHabit(Habit habit);
    List<JournalEntry> findByHabitAndEntryDateBetween(Habit habit, LocalDate startDate, LocalDate endDate);
    List<JournalEntry> findByHabitAndEntryDate(Habit habit, LocalDate date);
}
