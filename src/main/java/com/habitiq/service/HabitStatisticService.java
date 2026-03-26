package com.habitiq.service;

import com.habitiq.dto.HabitStatisticDto;
import com.habitiq.entity.Habit;
import com.habitiq.entity.HabitStatistic;
import com.habitiq.entity.User;
import com.habitiq.repository.HabitRepository;
import com.habitiq.repository.HabitStatisticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class HabitStatisticService {
    @Autowired private HabitStatisticRepository statisticRepository;
    @Autowired private HabitRepository habitRepository;
    @Autowired private UserService userService;

    public HabitStatisticDto.Response getStatisticByDate(Long habitId, LocalDate date) {
        User user = userService.getCurrentUser();
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));
        
        if (!habit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        HabitStatistic stat = statisticRepository.findByHabitAndDate(habit, date)
                .orElse(null);
        
        return stat != null ? toResponse(stat) : null;
    }

    public List<HabitStatisticDto.Response> getStatisticsByDateRange(Long habitId, LocalDate startDate, LocalDate endDate) {
        User user = userService.getCurrentUser();
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));
        
        if (!habit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        return statisticRepository.findByHabitAndDateBetween(habit, startDate, endDate).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public HabitStatisticDto.HabitStats getHabitStats(Long habitId) {
        User user = userService.getCurrentUser();
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));
        
        if (!habit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        List<HabitStatistic> stats = statisticRepository.findByHabit(habit);
        
        int totalDaysTracked = stats.size();
        int currentStreak = stats.isEmpty() ? 0 : stats.get(stats.size() - 1).getCurrentStreak();
        int longestStreak = stats.stream().mapToInt(HabitStatistic::getLongestStreak).max().orElse(0);
        int totalCompletions = stats.stream().mapToInt(HabitStatistic::getCompletionCount).sum();
        int totalMinutes = stats.stream().mapToInt(HabitStatistic::getTotalMinutes).sum();
        double completionRate = totalDaysTracked > 0 ? (totalCompletions * 100.0 / totalDaysTracked) : 0;

        List<HabitStatisticDto.HeatmapData> heatmapData = stats.stream()
                .map(stat -> new HabitStatisticDto.HeatmapData(
                        stat.getDate(),
                        Math.min(stat.getCompletionCount(), 4),
                        getIntensityLevel(stat.getCompletionCount())
                ))
                .collect(Collectors.toList());

        return new HabitStatisticDto.HabitStats(
                habitId,
                habit.getName(),
                totalDaysTracked,
                currentStreak,
                longestStreak,
                totalCompletions,
                totalMinutes,
                completionRate,
                heatmapData
        );
    }

    public void recordCompletion(Habit habit, Integer minutes, Boolean completed) {
        LocalDate today = LocalDate.now();
        HabitStatistic stat = statisticRepository.findByHabitAndDate(habit, today)
                .orElse(new HabitStatistic(null, habit, today, 0, 0, 0, 0, false, LocalDateTime.now(), LocalDateTime.now()));

        if (completed) {
            stat.setCompletionCount(stat.getCompletionCount() + 1);
            stat.setCompleted(true);
            stat.setCurrentStreak(stat.getCurrentStreak() + 1);
            if (stat.getCurrentStreak() > stat.getLongestStreak()) {
                stat.setLongestStreak(stat.getCurrentStreak());
            }
        } else {
            stat.setCurrentStreak(0);
        }

        stat.setTotalMinutes(stat.getTotalMinutes() + (minutes != null ? minutes : 0));
        stat.setUpdatedAt(LocalDateTime.now());

        statisticRepository.save(stat);
    }

    public void updateStreak(Habit habit) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<HabitStatistic> stats = statisticRepository.findByHabitAndDateBetween(habit, yesterday, LocalDate.now());
        
        HabitStatistic yesterdayStat = stats.stream()
                .filter(s -> s.getDate().equals(yesterday))
                .findFirst()
                .orElse(null);
        
        HabitStatistic todayStat = stats.stream()
                .filter(s -> s.getDate().equals(LocalDate.now()))
                .findFirst()
                .orElse(null);

        if (todayStat == null) {
            todayStat = new HabitStatistic();
            todayStat.setHabit(habit);
            todayStat.setDate(LocalDate.now());
            todayStat.setCurrentStreak(yesterdayStat != null ? yesterdayStat.getCurrentStreak() + 1 : 1);
            todayStat.setLongestStreak(yesterdayStat != null ? Math.max(yesterdayStat.getLongestStreak(), todayStat.getCurrentStreak()) : 1);
            statisticRepository.save(todayStat);
        }
    }

    private HabitStatisticDto.Response toResponse(HabitStatistic stat) {
        return new HabitStatisticDto.Response(
                stat.getId(),
                stat.getHabit().getId(),
                stat.getHabit().getName(),
                stat.getDate(),
                stat.getCompletionCount(),
                stat.getCurrentStreak(),
                stat.getLongestStreak(),
                stat.getTotalMinutes(),
                stat.getCompleted()
        );
    }

    private String getIntensityLevel(Integer count) {
        if (count >= 4) return "Very High";
        if (count >= 3) return "High";
        if (count >= 2) return "Medium";
        if (count >= 1) return "Low";
        return "None";
    }
}
