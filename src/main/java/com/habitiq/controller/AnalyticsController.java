package com.habitiq.controller;

import com.habitiq.dto.AnalyticsDto;
import com.habitiq.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/score")
    public ResponseEntity<AnalyticsDto.ProductivityScore> getProductivityScore() {
        return ResponseEntity.ok(analyticsService.getProductivityScore());
    }

    @GetMapping("/failure-risk")
    public ResponseEntity<List<AnalyticsDto.FailureRisk>> getFailureRisks() {
        return ResponseEntity.ok(analyticsService.getFailureRisks());
    }

    @GetMapping("/cluster")
    public ResponseEntity<AnalyticsDto.BehaviorCluster> getBehaviorCluster() {
        return ResponseEntity.ok(analyticsService.getBehaviorCluster());
    }

    @GetMapping("/trends")
    public ResponseEntity<List<AnalyticsDto.TrendData>> getTrends() {
        return ResponseEntity.ok(analyticsService.getTrends());
    }

    @GetMapping("/day-performance")
    public ResponseEntity<List<AnalyticsDto.DayPerformance>> getDayPerformance() {
        return ResponseEntity.ok(analyticsService.getDayPerformance());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsDto.DashboardSummary> getDashboardSummary() {
        return ResponseEntity.ok(analyticsService.getDashboardSummary());
    }

    @GetMapping("/weekly-reports")
    public ResponseEntity<List<AnalyticsDto.WeeklyReport>> getWeeklyReports() {
        return ResponseEntity.ok(analyticsService.getWeeklyReports());
    }
}
