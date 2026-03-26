package com.habitiq.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

public class AnalyticsDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProductivityScore {
        private Double dailyScore;
        private Double weeklyScore;
        private Double monthlyScore;
        private String date;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class FailureRisk {
        private Long habitId;
        private String habitName;
        private Double riskPercentage;
        private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
        private List<String> riskFactors;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class BehaviorCluster {
        private String clusterName;
        private String description;
        private Double confidenceScore;
        private Map<String, Object> details;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TrendData {
        private String period;
        private Double score;
        private String trend; // IMPROVING, DECLINING, STABLE
        private Double changePercentage;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DashboardSummary {
        private ProductivityScore productivityScore;
        private Integer totalHabits;
        private Integer completedToday;
        private Integer currentBestStreak;
        private String behaviorProfile;
        private List<FailureRisk> atRiskHabits;
        private Long unreadInsights;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DayPerformance {
        private String dayOfWeek;
        private Double completionRate;
        private Integer totalLogs;
        private Integer completedLogs;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class WeeklyReport {
        private String weekStart;
        private Double avgScore;
        private String trend;
        private String bestDay;
        private String worstDay;
        private Integer totalCompletions;
        private Integer totalPossible;
        private Double completionRate;
    }
}
