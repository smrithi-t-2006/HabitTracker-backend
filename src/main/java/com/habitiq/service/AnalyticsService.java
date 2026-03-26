package com.habitiq.service;

import com.habitiq.dto.AnalyticsDto;
import com.habitiq.entity.*;
import com.habitiq.exception.ResourceNotFoundException;
import com.habitiq.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;
    private final UserRepository userRepository;
    private final DailySummaryRepository dailySummaryRepository;
    private final WeeklySummaryRepository weeklySummaryRepository;
    private final StreakRepository streakRepository;
    private final InsightRepository insightRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // ==================== PRODUCTIVITY SCORE ====================

    public AnalyticsDto.ProductivityScore getProductivityScore() {
        User user = getCurrentUser();
        LocalDate today = LocalDate.now();

        double dailyScore = calculateScoreForRange(user.getId(), today, today);
        double weeklyScore = calculateScoreForRange(user.getId(), today.minusDays(6), today);
        double monthlyScore = calculateScoreForRange(user.getId(), today.minusDays(29), today);

        return AnalyticsDto.ProductivityScore.builder()
                .dailyScore(Math.round(dailyScore * 100.0) / 100.0)
                .weeklyScore(Math.round(weeklyScore * 100.0) / 100.0)
                .monthlyScore(Math.round(monthlyScore * 100.0) / 100.0)
                .date(today.toString())
                .build();
    }

    private double calculateScoreForRange(Long userId, LocalDate start, LocalDate end) {
        List<Habit> habits = habitRepository.findByUserIdAndIsActiveTrue(userId);
        if (habits.isEmpty()) return 0.0;

        double weightedCompleted = 0;
        double weightedTotal = 0;
        long days = start.until(end).getDays() + 1;

        for (Habit habit : habits) {
            int weight = habit.getDifficulty().getWeight();
            long completed = habitLogRepository.countCompletedByHabitAndDateRange(habit.getId(), start, end);
            weightedCompleted += completed * weight;
            weightedTotal += days * weight;
        }

        return weightedTotal == 0 ? 0 : (weightedCompleted / weightedTotal) * 100;
    }

    // ==================== FAILURE PROBABILITY ====================

    public List<AnalyticsDto.FailureRisk> getFailureRisks() {
        User user = getCurrentUser();
        List<Habit> habits = habitRepository.findByUserIdAndIsActiveTrue(user.getId());
        LocalDate today = LocalDate.now();

        return habits.stream().map(habit -> {
            List<String> riskFactors = new ArrayList<>();
            double risk = calculateFailureProbability(habit, today, riskFactors);

            String riskLevel;
            if (risk < 25) riskLevel = "LOW";
            else if (risk < 50) riskLevel = "MEDIUM";
            else if (risk < 75) riskLevel = "HIGH";
            else riskLevel = "CRITICAL";

            return AnalyticsDto.FailureRisk.builder()
                    .habitId(habit.getId())
                    .habitName(habit.getName())
                    .riskPercentage(Math.round(risk * 100.0) / 100.0)
                    .riskLevel(riskLevel)
                    .riskFactors(riskFactors)
                    .build();
        }).collect(Collectors.toList());
    }

    private double calculateFailureProbability(Habit habit, LocalDate today, List<String> riskFactors) {
        // Factor 1: 7-day completion rate (weight: 0.30)
        long completed7d = habitLogRepository.countCompletedByHabitAndDateRange(
                habit.getId(), today.minusDays(6), today);
        double rate7d = completed7d / 7.0;
        double factor1 = (1 - rate7d);
        if (rate7d < 0.5) riskFactors.add("Low 7-day completion rate (" + Math.round(rate7d * 100) + "%)");

        // Factor 2: Declining trend (weight: 0.25)
        long completedPrev7d = habitLogRepository.countCompletedByHabitAndDateRange(
                habit.getId(), today.minusDays(13), today.minusDays(7));
        double prevRate = completedPrev7d / 7.0;
        double trendDecline = Math.max(0, prevRate - rate7d);
        if (trendDecline > 0.1) riskFactors.add("Declining trend detected");

        // Factor 3: Streak break recency (weight: 0.25)
        Streak streak = streakRepository.findByHabitId(habit.getId()).orElse(null);
        double streakFactor = 0;
        if (streak != null && streak.getLastCompletedDate() != null) {
            long daysSinceLast = streak.getLastCompletedDate().until(today).getDays();
            if (daysSinceLast > 1) {
                streakFactor = Math.min(1.0, daysSinceLast / 7.0);
                riskFactors.add("Streak broken " + daysSinceLast + " days ago");
            }
        } else {
            streakFactor = 0.5;
        }

        // Factor 4: Day-of-week weakness (weight: 0.20)
        DayOfWeek currentDay = today.getDayOfWeek();
        double dayWeakness = calculateDayWeakness(habit.getId(), currentDay, today);
        if (dayWeakness > 0.5) riskFactors.add("Historically weak on " + currentDay.name().toLowerCase());

        return (0.30 * factor1 + 0.25 * trendDecline + 0.25 * streakFactor + 0.20 * dayWeakness) * 100;
    }

    private double calculateDayWeakness(Long habitId, DayOfWeek day, LocalDate today) {
        // Check last 4 weeks for this day of week
        int completed = 0;
        int total = 0;
        for (int i = 0; i < 4; i++) {
            LocalDate targetDate = today.minusWeeks(i);
            while (targetDate.getDayOfWeek() != day) {
                targetDate = targetDate.minusDays(1);
            }
            total++;
            long count = habitLogRepository.countCompletedByHabitAndDateRange(habitId, targetDate, targetDate);
            if (count > 0) completed++;
        }
        return total == 0 ? 0 : 1.0 - ((double) completed / total);
    }

    // ==================== BEHAVIOR CLUSTERING ====================

    public AnalyticsDto.BehaviorCluster getBehaviorCluster() {
        User user = getCurrentUser();
        LocalDate today = LocalDate.now();
        List<Habit> habits = habitRepository.findByUserIdAndIsActiveTrue(user.getId());

        if (habits.isEmpty()) {
            return AnalyticsDto.BehaviorCluster.builder()
                    .clusterName("New User")
                    .description("Start tracking habits to see your behavior profile!")
                    .confidenceScore(0.0)
                    .details(Map.of())
                    .build();
        }

        // Calculate metrics
        double weekdayRate = calculateWeekdayRate(user.getId(), today);
        double weekendRate = calculateWeekendRate(user.getId(), today);
        double overallRate7d = calculateScoreForRange(user.getId(), today.minusDays(6), today) / 100.0;
        double overallRate14d = calculateScoreForRange(user.getId(), today.minusDays(13), today) / 100.0;
        double firstHalfRate = calculateFirstHalfRate(user.getId(), today);
        double secondHalfRate = calculateSecondHalfRate(user.getId(), today);

        // Check trend
        boolean declining = overallRate7d < overallRate14d - 0.1;

        Map<String, Object> details = new HashMap<>();
        details.put("weekdayRate", Math.round(weekdayRate * 100.0) / 100.0);
        details.put("weekendRate", Math.round(weekendRate * 100.0) / 100.0);
        details.put("overallRate7d", Math.round(overallRate7d * 100.0) / 100.0);

        // Classify
        if (overallRate7d >= 0.85 && !declining) {
            return AnalyticsDto.BehaviorCluster.builder()
                    .clusterName("Consistent Achiever")
                    .description("You maintain excellent consistency! Keep up the great work.")
                    .confidenceScore(overallRate7d)
                    .details(details)
                    .build();
        } else if (weekendRate > weekdayRate + 0.30) {
            return AnalyticsDto.BehaviorCluster.builder()
                    .clusterName("Weekend Warrior")
                    .description("You perform much better on weekends. Try to build weekday routines!")
                    .confidenceScore(weekendRate - weekdayRate)
                    .details(details)
                    .build();
        } else if (secondHalfRate > firstHalfRate + 0.25) {
            return AnalyticsDto.BehaviorCluster.builder()
                    .clusterName("Slow Starter")
                    .description("You tend to pick up momentum as the week progresses.")
                    .confidenceScore(secondHalfRate - firstHalfRate)
                    .details(details)
                    .build();
        } else if (declining) {
            return AnalyticsDto.BehaviorCluster.builder()
                    .clusterName("Declining")
                    .description("Your performance has been declining. Consider reducing habit load.")
                    .confidenceScore(overallRate14d - overallRate7d)
                    .details(details)
                    .build();
        } else {
            return AnalyticsDto.BehaviorCluster.builder()
                    .clusterName("Steady Progress")
                    .description("You're making steady progress. Push a bit more to reach Consistent Achiever!")
                    .confidenceScore(overallRate7d)
                    .details(details)
                    .build();
        }
    }

    private double calculateWeekdayRate(Long userId, LocalDate today) {
        List<Habit> habits = habitRepository.findByUserIdAndIsActiveTrue(userId);
        int completed = 0, total = 0;
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.minusDays(i);
            if (date.getDayOfWeek().getValue() <= 5) { // Mon-Fri
                for (Habit h : habits) {
                    total++;
                    if (habitLogRepository.countCompletedByHabitAndDateRange(h.getId(), date, date) > 0)
                        completed++;
                }
            }
        }
        return total == 0 ? 0 : (double) completed / total;
    }

    private double calculateWeekendRate(Long userId, LocalDate today) {
        List<Habit> habits = habitRepository.findByUserIdAndIsActiveTrue(userId);
        int completed = 0, total = 0;
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.minusDays(i);
            if (date.getDayOfWeek().getValue() > 5) { // Sat-Sun
                for (Habit h : habits) {
                    total++;
                    if (habitLogRepository.countCompletedByHabitAndDateRange(h.getId(), date, date) > 0)
                        completed++;
                }
            }
        }
        return total == 0 ? 0 : (double) completed / total;
    }

    private double calculateFirstHalfRate(Long userId, LocalDate today) {
        // Mon-Wed of current week
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate midWeek = weekStart.plusDays(2);
        if (midWeek.isAfter(today)) midWeek = today;
        return calculateScoreForRange(userId, weekStart, midWeek) / 100.0;
    }

    private double calculateSecondHalfRate(Long userId, LocalDate today) {
        // Thu-Sun of current week
        LocalDate thursday = today.with(DayOfWeek.THURSDAY);
        if (thursday.isAfter(today)) return 0;
        return calculateScoreForRange(userId, thursday, today) / 100.0;
    }

    // ==================== TRENDS ====================

    public List<AnalyticsDto.TrendData> getTrends() {
        User user = getCurrentUser();
        LocalDate today = LocalDate.now();
        List<AnalyticsDto.TrendData> trends = new ArrayList<>();

        for (int i = 3; i >= 0; i--) {
            LocalDate weekEnd = today.minusWeeks(i);
            LocalDate weekStart = weekEnd.minusDays(6);
            double score = calculateScoreForRange(user.getId(), weekStart, weekEnd);

            String trend = "STABLE";
            double change = 0;
            if (!trends.isEmpty()) {
                double prevScore = trends.get(trends.size() - 1).getScore();
                change = score - prevScore;
                if (change > 5) trend = "IMPROVING";
                else if (change < -5) trend = "DECLINING";
            }

            trends.add(AnalyticsDto.TrendData.builder()
                    .period(weekStart + " to " + weekEnd)
                    .score(Math.round(score * 100.0) / 100.0)
                    .trend(trend)
                    .changePercentage(Math.round(change * 100.0) / 100.0)
                    .build());
        }

        return trends;
    }

    // ==================== DAY PERFORMANCE ====================

    public List<AnalyticsDto.DayPerformance> getDayPerformance() {
        User user = getCurrentUser();
        LocalDate today = LocalDate.now();
        List<Habit> habits = habitRepository.findByUserIdAndIsActiveTrue(user.getId());

        Map<DayOfWeek, int[]> dayStats = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            dayStats.put(day, new int[]{0, 0}); // [completed, total]
        }

        // Look at last 28 days
        for (int i = 0; i < 28; i++) {
            LocalDate date = today.minusDays(i);
            DayOfWeek day = date.getDayOfWeek();
            for (Habit h : habits) {
                dayStats.get(day)[1]++;
                if (habitLogRepository.countCompletedByHabitAndDateRange(h.getId(), date, date) > 0) {
                    dayStats.get(day)[0]++;
                }
            }
        }

        return dayStats.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getKey().getValue()))
                .map(e -> AnalyticsDto.DayPerformance.builder()
                        .dayOfWeek(e.getKey().name())
                        .completedLogs(e.getValue()[0])
                        .totalLogs(e.getValue()[1])
                        .completionRate(e.getValue()[1] == 0 ? 0 :
                                Math.round((double) e.getValue()[0] / e.getValue()[1] * 10000.0) / 100.0)
                        .build())
                .collect(Collectors.toList());
    }

    // ==================== DASHBOARD SUMMARY ====================

    public AnalyticsDto.DashboardSummary getDashboardSummary() {
        User user = getCurrentUser();
        LocalDate today = LocalDate.now();
        List<Habit> habits = habitRepository.findByUserIdAndIsActiveTrue(user.getId());
        List<HabitLog> todayLogs = habitLogRepository.findByUserIdAndDate(user.getId(), today);

        long completedToday = todayLogs.stream().filter(HabitLog::getCompleted).count();

        int bestStreak = habits.stream()
                .map(h -> streakRepository.findByHabitId(h.getId()).map(Streak::getCurrentStreak).orElse(0))
                .max(Integer::compareTo).orElse(0);

        AnalyticsDto.ProductivityScore score = getProductivityScore();
        AnalyticsDto.BehaviorCluster cluster = getBehaviorCluster();

        List<AnalyticsDto.FailureRisk> atRisk = getFailureRisks().stream()
                .filter(r -> r.getRiskPercentage() > 50)
                .collect(Collectors.toList());

        Long unreadInsights = insightRepository.countByUserIdAndIsReadFalse(user.getId());

        return AnalyticsDto.DashboardSummary.builder()
                .productivityScore(score)
                .totalHabits(habits.size())
                .completedToday((int) completedToday)
                .currentBestStreak(bestStreak)
                .behaviorProfile(cluster.getClusterName())
                .atRiskHabits(atRisk)
                .unreadInsights(unreadInsights)
                .build();
    }

    // ==================== WEEKLY REPORTS ====================

    public List<AnalyticsDto.WeeklyReport> getWeeklyReports() {
        User user = getCurrentUser();
        return weeklySummaryRepository.findTop4ByUserIdOrderByWeekStartDesc(user.getId())
                .stream().map(ws -> AnalyticsDto.WeeklyReport.builder()
                        .weekStart(ws.getWeekStart().toString())
                        .avgScore(ws.getAvgScore())
                        .trend(ws.getTrend())
                        .bestDay(ws.getBestDay())
                        .worstDay(ws.getWorstDay())
                        .totalCompletions(ws.getTotalCompletions())
                        .totalPossible(ws.getTotalPossible())
                        .completionRate(ws.getTotalPossible() == 0 ? 0 :
                                Math.round((double) ws.getTotalCompletions() / ws.getTotalPossible() * 10000.0) / 100.0)
                        .build())
                .collect(Collectors.toList());
    }
}
