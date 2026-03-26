package com.habitiq.dto;

import com.habitiq.entity.Goal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

public class GoalDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        @NotNull
        private Long habitId;
        @NotBlank
        private String title;
        private String description;
        @NotNull
        private Double targetValue;
        @NotBlank
        private String unit; // e.g., "km", "pages", "hours"
        private LocalDateTime startDate;
        private LocalDateTime dueDate;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class UpdateRequest {
        private String title;
        private String description;
        private Double targetValue;
        private String unit;
        private Double currentProgress;
        private LocalDateTime dueDate;
        private Goal.GoalStatus status;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long habitId;
        private String habitName;
        private String title;
        private String description;
        private Double targetValue;
        private String unit;
        private Double currentProgress;
        private Double progressPercentage;
        private LocalDateTime startDate;
        private LocalDateTime dueDate;
        private Goal.GoalStatus status;
        private Boolean isCompleted;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ProgressUpdate {
        @NotNull
        private Double progressValue;
    }
}
