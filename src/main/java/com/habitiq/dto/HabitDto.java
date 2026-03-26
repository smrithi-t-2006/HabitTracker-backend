package com.habitiq.dto;

import com.habitiq.entity.Habit;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class HabitDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank
        private String name;
        private String description;
        private Long categoryId;
        private Habit.Difficulty difficulty = Habit.Difficulty.MEDIUM;
        private Habit.Frequency frequency = Habit.Frequency.DAILY;
        private String color = "#4CAF50";
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class UpdateRequest {
        private String name;
        private String description;
        private Long categoryId;
        private Habit.Difficulty difficulty;
        private Habit.Frequency frequency;
        private String color;
        private Boolean isActive;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private Long categoryId;
        private String categoryName;
        private Habit.Difficulty difficulty;
        private Habit.Frequency frequency;
        private String color;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private StreakInfo streak;
        private ReminderInfo reminder;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StreakInfo {
        private Integer currentStreak;
        private Integer longestStreak;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ReminderInfo {
        private LocalTime reminderTime;
        private String message;
        private Boolean isEnabled;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class LogRequest {
        private Boolean completed = true;
        private String notes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LogResponse {
        private Long id;
        private Long habitId;
        private String habitName;
        private String logDate;
        private Boolean completed;
        private String completedAt;
        private String notes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ReminderRequest {
        private LocalTime reminderTime;
        private String message;
        private Boolean isEnabled = true;
    }
}
