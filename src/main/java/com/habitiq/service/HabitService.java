package com.habitiq.service;

import com.habitiq.dto.HabitDto;
import com.habitiq.entity.*;
import com.habitiq.exception.BadRequestException;
import com.habitiq.exception.ResourceNotFoundException;
import com.habitiq.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitRepository habitRepository;
    private final UserRepository userRepository;
    private final HabitLogRepository habitLogRepository;
    private final StreakRepository streakRepository;
    private final ReminderRepository reminderRepository;
    private final CategoryRepository categoryRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public HabitDto.Response createHabit(HabitDto.CreateRequest request) {
        User user = getCurrentUser();
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }
        Habit habit = Habit.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .category(category)
                .difficulty(request.getDifficulty())
                .frequency(request.getFrequency())
                .color(request.getColor())
                .build();
        habit = habitRepository.save(habit);

        // Initialize streak
        Streak streak = Streak.builder().habit(habit).build();
        streakRepository.save(streak);

        return toResponse(habit);
    }

    public List<HabitDto.Response> getUserHabits() {
        User user = getCurrentUser();
        return habitRepository.findByUserIdAndIsActiveTrue(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public HabitDto.Response getHabit(Long id) {
        Habit habit = findHabitForCurrentUser(id);
        return toResponse(habit);
    }

    @Transactional
    public HabitDto.Response updateHabit(Long id, HabitDto.UpdateRequest request) {
        Habit habit = findHabitForCurrentUser(id);
        if (request.getName() != null) habit.setName(request.getName());
        if (request.getDescription() != null) habit.setDescription(request.getDescription());
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            habit.setCategory(category);
        }
        if (request.getDifficulty() != null) habit.setDifficulty(request.getDifficulty());
        if (request.getFrequency() != null) habit.setFrequency(request.getFrequency());
        if (request.getColor() != null) habit.setColor(request.getColor());
        if (request.getIsActive() != null) habit.setIsActive(request.getIsActive());
        habit = habitRepository.save(habit);
        return toResponse(habit);
    }

    @Transactional
    public void deleteHabit(Long id) {
        Habit habit = findHabitForCurrentUser(id);
        habitRepository.delete(habit);
    }

    @Transactional
    public HabitDto.LogResponse logHabit(Long habitId, HabitDto.LogRequest request) {
        Habit habit = findHabitForCurrentUser(habitId);
        LocalDate today = LocalDate.now();

        HabitLog log = habitLogRepository.findByHabitIdAndLogDate(habitId, today)
                .orElse(HabitLog.builder().habit(habit).logDate(today).build());

        log.setCompleted(request.getCompleted());
        log.setNotes(request.getNotes());
        if (request.getCompleted()) {
            log.setCompletedAt(LocalDateTime.now());
        } else {
            log.setCompletedAt(null);
        }
        log = habitLogRepository.save(log);

        // Update streak
        updateStreak(habit, request.getCompleted());

        return HabitDto.LogResponse.builder()
                .id(log.getId())
                .habitId(habitId)
                .habitName(habit.getName())
                .logDate(log.getLogDate().toString())
                .completed(log.getCompleted())
                .completedAt(log.getCompletedAt() != null ? log.getCompletedAt().toString() : null)
                .notes(log.getNotes())
                .build();
    }

    public List<HabitDto.LogResponse> getHabitLogs(Long habitId, LocalDate start, LocalDate end) {
        findHabitForCurrentUser(habitId);
        return habitLogRepository.findByHabitIdAndLogDateBetween(habitId, start, end)
                .stream().map(log -> HabitDto.LogResponse.builder()
                        .id(log.getId())
                        .habitId(habitId)
                        .habitName(log.getHabit().getName())
                        .logDate(log.getLogDate().toString())
                        .completed(log.getCompleted())
                        .completedAt(log.getCompletedAt() != null ? log.getCompletedAt().toString() : null)
                        .notes(log.getNotes())
                        .build())
                .collect(Collectors.toList());
    }

    public List<HabitDto.LogResponse> getTodayLogs() {
        User user = getCurrentUser();
        LocalDate today = LocalDate.now();
        List<Habit> habits = habitRepository.findByUserIdAndIsActiveTrue(user.getId());
        List<HabitLog> todayLogs = habitLogRepository.findByUserIdAndDate(user.getId(), today);

        return habits.stream().map(habit -> {
            HabitLog log = todayLogs.stream()
                    .filter(l -> l.getHabit().getId().equals(habit.getId()))
                    .findFirst().orElse(null);
            return HabitDto.LogResponse.builder()
                    .habitId(habit.getId())
                    .habitName(habit.getName())
                    .logDate(today.toString())
                    .completed(log != null && log.getCompleted())
                    .completedAt(log != null && log.getCompletedAt() != null ? log.getCompletedAt().toString() : null)
                    .notes(log != null ? log.getNotes() : null)
                    .build();
        }).collect(Collectors.toList());
    }

    // --- Reminder ---

    @Transactional
    public HabitDto.ReminderInfo setReminder(Long habitId, HabitDto.ReminderRequest request) {
        Habit habit = findHabitForCurrentUser(habitId);
        Reminder reminder = reminderRepository.findByHabitId(habitId)
                .orElse(Reminder.builder().habit(habit).build());
        reminder.setReminderTime(request.getReminderTime());
        reminder.setMessage(request.getMessage());
        reminder.setIsEnabled(request.getIsEnabled());
        reminderRepository.save(reminder);
        return HabitDto.ReminderInfo.builder()
                .reminderTime(reminder.getReminderTime())
                .message(reminder.getMessage())
                .isEnabled(reminder.getIsEnabled())
                .build();
    }

    // --- Private helpers ---

    private Habit findHabitForCurrentUser(Long habitId) {
        User user = getCurrentUser();
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found"));
        if (!habit.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Access denied to this habit");
        }
        return habit;
    }

    private void updateStreak(Habit habit, boolean completed) {
        Streak streak = streakRepository.findByHabitId(habit.getId())
                .orElse(Streak.builder().habit(habit).build());
        LocalDate today = LocalDate.now();

        if (completed) {
            if (streak.getLastCompletedDate() == null ||
                streak.getLastCompletedDate().equals(today.minusDays(1))) {
                streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            } else if (!streak.getLastCompletedDate().equals(today)) {
                streak.setCurrentStreak(1);
            }
            streak.setLastCompletedDate(today);
            if (streak.getCurrentStreak() > streak.getLongestStreak()) {
                streak.setLongestStreak(streak.getCurrentStreak());
            }
        }
        streakRepository.save(streak);
    }

    private HabitDto.Response toResponse(Habit habit) {
        HabitDto.Response.ResponseBuilder builder = HabitDto.Response.builder()
                .id(habit.getId())
                .name(habit.getName())
                .description(habit.getDescription())
                .categoryId(habit.getCategory() != null ? habit.getCategory().getId() : null)
                .categoryName(habit.getCategory() != null ? habit.getCategory().getName() : null)
                .difficulty(habit.getDifficulty())
                .frequency(habit.getFrequency())
                .color(habit.getColor())
                .isActive(habit.getIsActive())
                .createdAt(habit.getCreatedAt());

        Streak streak = streakRepository.findByHabitId(habit.getId()).orElse(null);
        if (streak != null) {
            builder.streak(HabitDto.StreakInfo.builder()
                    .currentStreak(streak.getCurrentStreak())
                    .longestStreak(streak.getLongestStreak())
                    .build());
        }

        Reminder reminder = reminderRepository.findByHabitId(habit.getId()).orElse(null);
        if (reminder != null) {
            builder.reminder(HabitDto.ReminderInfo.builder()
                    .reminderTime(reminder.getReminderTime())
                    .message(reminder.getMessage())
                    .isEnabled(reminder.getIsEnabled())
                    .build());
        }

        return builder.build();
    }
}
