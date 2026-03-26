package com.habitiq.scheduler;

import com.habitiq.entity.*;
import com.habitiq.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataAggregationJob {

    private final UserRepository userRepository;
    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;
    private final DailySummaryRepository dailySummaryRepository;
    private final WeeklySummaryRepository weeklySummaryRepository;
    private final StreakRepository streakRepository;

    /**
     * Runs every day at 11:55 PM to aggregate daily data
     */
    @Scheduled(cron = "0 55 23 * * *")
    @Transactional
    public void aggregateDailyData() {
        log.info("Running daily data aggregation...");
        LocalDate today = LocalDate.now();
        List<User> users = userRepository.findAll();

        for (User user : users) {
            List<Habit> habits = habitRepository.findByUserIdAndIsActiveTrue(user.getId());
            if (habits.isEmpty()) continue;

            double weightedCompleted = 0;
            double weightedTotal = 0;
            int completed = 0;

            for (Habit habit : habits) {
                int weight = habit.getDifficulty().getWeight();
                long comp = habitLogRepository.countCompletedByHabitAndDateRange(habit.getId(), today, today);
                if (comp > 0) {
                    weightedCompleted += weight;
                    completed++;
                }
                weightedTotal += weight;
            }

            double score = weightedTotal == 0 ? 0 : (weightedCompleted / weightedTotal) * 100;

            DailySummary summary = dailySummaryRepository.findByUserIdAndSummaryDate(user.getId(), today)
                    .orElse(DailySummary.builder().user(user).summaryDate(today).build());
            summary.setProductivityScore(Math.round(score * 100.0) / 100.0);
            summary.setTotalHabits(habits.size());
            summary.setCompletedHabits(completed);
            dailySummaryRepository.save(summary);

            // Reset broken streaks
            for (Habit habit : habits) {
                Streak streak = streakRepository.findByHabitId(habit.getId()).orElse(null);
                if (streak != null && streak.getLastCompletedDate() != null) {
                    long daysSinceLast = streak.getLastCompletedDate().until(today).getDays();
                    if (daysSinceLast > 1) {
                        streak.setCurrentStreak(0);
                        streakRepository.save(streak);
                    }
                }
            }
        }
        log.info("Daily data aggregation completed.");
    }

    /**
     * Runs every Sunday at 11:50 PM to aggregate weekly data
     */
    @Scheduled(cron = "0 50 23 * * SUN")
    @Transactional
    public void aggregateWeeklyData() {
        log.info("Running weekly data aggregation...");
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        List<User> users = userRepository.findAll();

        for (User user : users) {
            List<Habit> habits = habitRepository.findByUserIdAndIsActiveTrue(user.getId());
            if (habits.isEmpty()) continue;

            Map<DayOfWeek, int[]> dayStats = new EnumMap<>(DayOfWeek.class);
            for (DayOfWeek day : DayOfWeek.values()) {
                dayStats.put(day, new int[]{0, 0});
            }

            int totalCompletions = 0;
            int totalPossible = 0;
            double totalScore = 0;
            int days = 0;

            for (LocalDate date = weekStart; !date.isAfter(today); date = date.plusDays(1)) {
                days++;
                for (Habit habit : habits) {
                    totalPossible++;
                    long comp = habitLogRepository.countCompletedByHabitAndDateRange(habit.getId(), date, date);
                    DayOfWeek dow = date.getDayOfWeek();
                    dayStats.get(dow)[1]++;
                    if (comp > 0) {
                        totalCompletions++;
                        dayStats.get(dow)[0]++;
                    }
                }
                DailySummary ds = dailySummaryRepository.findByUserIdAndSummaryDate(user.getId(), date).orElse(null);
                if (ds != null) totalScore += ds.getProductivityScore();
            }

            double avgScore = days == 0 ? 0 : totalScore / days;

            // Find best and worst days
            String bestDay = null, worstDay = null;
            double bestRate = -1, worstRate = 2;
            for (Map.Entry<DayOfWeek, int[]> e : dayStats.entrySet()) {
                if (e.getValue()[1] > 0) {
                    double rate = (double) e.getValue()[0] / e.getValue()[1];
                    if (rate > bestRate) { bestRate = rate; bestDay = e.getKey().name(); }
                    if (rate < worstRate) { worstRate = rate; worstDay = e.getKey().name(); }
                }
            }

            // Determine trend
            Optional<WeeklySummary> prevWeek = weeklySummaryRepository
                    .findByUserIdAndWeekStart(user.getId(), weekStart.minusWeeks(1));
            String trend = "STABLE";
            if (prevWeek.isPresent()) {
                double diff = avgScore - prevWeek.get().getAvgScore();
                if (diff > 5) trend = "IMPROVING";
                else if (diff < -5) trend = "DECLINING";
            }

            WeeklySummary ws = weeklySummaryRepository.findByUserIdAndWeekStart(user.getId(), weekStart)
                    .orElse(WeeklySummary.builder().user(user).weekStart(weekStart).build());
            ws.setAvgScore(Math.round(avgScore * 100.0) / 100.0);
            ws.setTrend(trend);
            ws.setBestDay(bestDay);
            ws.setWorstDay(worstDay);
            ws.setTotalCompletions(totalCompletions);
            ws.setTotalPossible(totalPossible);
            weeklySummaryRepository.save(ws);
        }
        log.info("Weekly data aggregation completed.");
    }
}
