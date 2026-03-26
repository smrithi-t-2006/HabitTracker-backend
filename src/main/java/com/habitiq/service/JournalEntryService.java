package com.habitiq.service;

import com.habitiq.dto.JournalEntryDto;
import com.habitiq.dto.JournalEntryDto.CreateRequest;
import com.habitiq.dto.JournalEntryDto.UpdateRequest;
import com.habitiq.entity.Habit;
import com.habitiq.entity.JournalEntry;
import com.habitiq.entity.User;
import com.habitiq.repository.HabitRepository;
import com.habitiq.repository.JournalEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class JournalEntryService {
    @Autowired private JournalEntryRepository journalEntryRepository;
    @Autowired private HabitRepository habitRepository;
    @Autowired private UserService userService;

    public List<JournalEntryDto.Response> getHabitEntries(Long habitId) {
        User user = userService.getCurrentUser();
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));
        
        if (!habit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        return journalEntryRepository.findByHabit(habit).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<JournalEntryDto.Response> getEntriesByDateRange(Long habitId, LocalDate startDate, LocalDate endDate) {
        User user = userService.getCurrentUser();
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));
        
        if (!habit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        return journalEntryRepository.findByHabitAndEntryDateBetween(habit, startDate, endDate).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<JournalEntryDto.Response> getEntriesByDate(Long habitId, LocalDate date) {
        User user = userService.getCurrentUser();
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));
        
        if (!habit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        return journalEntryRepository.findByHabitAndEntryDate(habit, date).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public JournalEntryDto.Response createEntry(CreateRequest request) {
        User user = userService.getCurrentUser();
        Habit habit = habitRepository.findById(request.getHabitId())
                .orElseThrow(() -> new RuntimeException("Habit not found"));
        
        if (!habit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        JournalEntry entry = new JournalEntry();
        entry.setHabit(habit);
        entry.setTitle(request.getTitle());
        entry.setContent(request.getContent());
        entry.setMood(request.getMood());
        entry.setEntryDate(request.getEntryDate() != null ? request.getEntryDate() : LocalDate.now());
        entry.setDuration(request.getDuration() != null ? request.getDuration() : 0);
        entry.setTags(request.getTags());

        return toResponse(journalEntryRepository.save(entry));
    }

    public JournalEntryDto.Response updateEntry(Long id, UpdateRequest request) {
        User user = userService.getCurrentUser();
        JournalEntry entry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));
        
        if (!entry.getHabit().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        entry.setTitle(request.getTitle());
        entry.setContent(request.getContent());
        entry.setMood(request.getMood());
        entry.setDuration(request.getDuration());
        entry.setTags(request.getTags());
        entry.setUpdatedAt(LocalDateTime.now());

        return toResponse(journalEntryRepository.save(entry));
    }

    public void deleteEntry(Long id) {
        User user = userService.getCurrentUser();
        JournalEntry entry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));
        
        if (!entry.getHabit().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        journalEntryRepository.deleteById(id);
    }

    private JournalEntryDto.Response toResponse(JournalEntry entry) {
        return new JournalEntryDto.Response(
                entry.getId(),
                entry.getHabit().getId(),
                entry.getHabit().getName(),
                entry.getTitle(),
                entry.getContent(),
                entry.getMood(),
                entry.getEntryDate(),
                entry.getDuration(),
                entry.getTags(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }
}
