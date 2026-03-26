package com.habitiq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class HabitStatisticDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayStatistic {
        private LocalDate date;
        private Integer completionCount;
        private Integer currentStreak;
        private Integer totalMinutes;
        private Boolean completed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeatmapData {
        private LocalDate date;
        private Integer intensity;
        private String level;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HabitStats {
        private Long habitId;
        private String habitName;
        private Integer totalDaysTracked;
        private Integer currentStreak;
        private Integer longestStreak;
        private Integer totalCompletions;
        private Integer totalMinutes;
        private Double completionRate;
        private List<HeatmapData> heatmapData;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long habitId;
        private String habitName;
        private LocalDate date;
        private Integer completionCount;
        private Integer currentStreak;
        private Integer longestStreak;
        private Integer totalMinutes;
        private Boolean completed;
    }
}
