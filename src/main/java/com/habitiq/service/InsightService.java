package com.habitiq.service;

import com.habitiq.dto.AnalyticsDto;
import com.habitiq.dto.InsightDto;
import com.habitiq.entity.*;
import com.habitiq.exception.ResourceNotFoundException;
import com.habitiq.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsightService {

    private final InsightRepository insightRepository;
    private final UserRepository userRepository;
    private final AnalyticsService analyticsService;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public List<InsightDto.Response> getInsights() {
        User user = getCurrentUser();
        return insightRepository.findByUserIdOrderByGeneratedAtDesc(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<InsightDto.Response> getUnreadInsights() {
        User user = getCurrentUser();
        return insightRepository.findByUserIdAndIsReadFalseOrderByGeneratedAtDesc(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long insightId) {
        Insight insight = insightRepository.findById(insightId)
                .orElseThrow(() -> new ResourceNotFoundException("Insight not found"));
        insight.setIsRead(true);
        insightRepository.save(insight);
    }

    @Transactional
    public void markAllAsRead() {
        User user = getCurrentUser();
        List<Insight> unread = insightRepository.findByUserIdAndIsReadFalseOrderByGeneratedAtDesc(user.getId());
        unread.forEach(i -> i.setIsRead(true));
        insightRepository.saveAll(unread);
    }

    @Transactional
    public List<InsightDto.Response> generateInsights() {
        User user = getCurrentUser();
        List<Insight> newInsights = new ArrayList<>();

        // Generate risk alerts
        List<AnalyticsDto.FailureRisk> risks = analyticsService.getFailureRisks();
        for (AnalyticsDto.FailureRisk risk : risks) {
            if (risk.getRiskPercentage() > 60) {
                newInsights.add(Insight.builder()
                        .user(user)
                        .type(Insight.InsightType.RISK_ALERT)
                        .message("⚠️ Your habit '" + risk.getHabitName() + "' has a " +
                                Math.round(risk.getRiskPercentage()) + "% failure risk. " +
                                String.join(". ", risk.getRiskFactors()) + ".")
                        .build());
            }
        }

        // Generate progress advice
        AnalyticsDto.ProductivityScore score = analyticsService.getProductivityScore();
        if (score.getWeeklyScore() > score.getMonthlyScore() + 10) {
            newInsights.add(Insight.builder()
                    .user(user)
                    .type(Insight.InsightType.PROGRESS)
                    .message("📈 Great improvement! Your weekly score (" + Math.round(score.getWeeklyScore()) +
                            "%) is higher than your monthly average (" + Math.round(score.getMonthlyScore()) + "%).")
                    .build());
        } else if (score.getWeeklyScore() < score.getMonthlyScore() - 10) {
            newInsights.add(Insight.builder()
                    .user(user)
                    .type(Insight.InsightType.PROGRESS)
                    .message("📉 Your weekly score (" + Math.round(score.getWeeklyScore()) +
                            "%) is below your monthly average (" + Math.round(score.getMonthlyScore()) +
                            "%). Try to get back on track!")
                    .build());
        }

        // Generate personalized tips based on behavior
        AnalyticsDto.BehaviorCluster cluster = analyticsService.getBehaviorCluster();
        switch (cluster.getClusterName()) {
            case "Weekend Warrior":
                newInsights.add(Insight.builder()
                        .user(user)
                        .type(Insight.InsightType.TIP)
                        .message("💡 You perform better on weekends. Try setting morning reminders on weekdays to build consistency.")
                        .build());
                break;
            case "Slow Starter":
                newInsights.add(Insight.builder()
                        .user(user)
                        .type(Insight.InsightType.TIP)
                        .message("💡 You tend to pick up momentum later in the week. Try starting with your easiest habit on Monday!")
                        .build());
                break;
            case "Declining":
                newInsights.add(Insight.builder()
                        .user(user)
                        .type(Insight.InsightType.ADAPTIVE_SUGGESTION)
                        .message("🔄 Your performance is declining. Consider reducing the number of habits or lowering difficulty levels temporarily.")
                        .build());
                break;
            case "Consistent Achiever":
                newInsights.add(Insight.builder()
                        .user(user)
                        .type(Insight.InsightType.TIP)
                        .message("🌟 Amazing consistency! Consider adding a new challenging habit or increasing difficulty of existing ones.")
                        .build());
                break;
            default:
                newInsights.add(Insight.builder()
                        .user(user)
                        .type(Insight.InsightType.TIP)
                        .message("💡 Keep going! Focus on completing at least 80% of your daily habits to become a Consistent Achiever.")
                        .build());
        }

        // Generate day-specific tips
        List<AnalyticsDto.DayPerformance> dayPerf = analyticsService.getDayPerformance();
        dayPerf.stream()
                .filter(d -> d.getCompletionRate() < 40 && d.getTotalLogs() > 0)
                .findFirst()
                .ifPresent(weakDay -> newInsights.add(Insight.builder()
                        .user(user)
                        .type(Insight.InsightType.ADAPTIVE_SUGGESTION)
                        .message("📊 " + weakDay.getDayOfWeek() + " is your weakest day (" +
                                Math.round(weakDay.getCompletionRate()) + "% completion rate). " +
                                "Consider setting extra reminders or simplifying your routine on that day.")
                        .build()));

        insightRepository.saveAll(newInsights);
        return newInsights.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private InsightDto.Response toResponse(Insight insight) {
        return InsightDto.Response.builder()
                .id(insight.getId())
                .type(insight.getType())
                .message(insight.getMessage())
                .generatedAt(insight.getGeneratedAt())
                .isRead(insight.getIsRead())
                .build();
    }
}
