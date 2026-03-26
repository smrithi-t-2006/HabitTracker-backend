package com.habitiq.dto;

import com.habitiq.entity.Habit;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

public class HabitTemplateDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank
        private String name;
        private String description;
        @NotBlank
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
        private Boolean isPopular;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private Long categoryId;
        private String categoryName;
        private Habit.Difficulty difficulty;
        private Habit.Frequency frequency;
        private String color;
        private Boolean isPopular;
        private Integer usageCount;
        private LocalDateTime createdAt;
    }
}
