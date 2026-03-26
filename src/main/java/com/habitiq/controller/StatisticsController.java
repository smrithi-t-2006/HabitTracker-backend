package com.habitiq.controller;

import com.habitiq.dto.HabitStatisticDto;
import com.habitiq.service.HabitStatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    @Autowired private HabitStatisticService statisticService;

    @GetMapping("/habit/{habitId}/date/{date}")
    public ResponseEntity<HabitStatisticDto.Response> getStatisticByDate(
            @PathVariable Long habitId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(statisticService.getStatisticByDate(habitId, date));
    }

    @GetMapping("/habit/{habitId}/range")
    public ResponseEntity<List<HabitStatisticDto.Response>> getStatisticsByDateRange(
            @PathVariable Long habitId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(statisticService.getStatisticsByDateRange(habitId, startDate, endDate));
    }

    @GetMapping("/habit/{habitId}/stats")
    public ResponseEntity<HabitStatisticDto.HabitStats> getHabitStats(@PathVariable Long habitId) {
        return ResponseEntity.ok(statisticService.getHabitStats(habitId));
    }

    @GetMapping("/habit/{habitId}/heatmap")
    public ResponseEntity<List<HabitStatisticDto.HeatmapData>> getHeatmapData(@PathVariable Long habitId) {
        HabitStatisticDto.HabitStats stats = statisticService.getHabitStats(habitId);
        return ResponseEntity.ok(stats.getHeatmapData());
    }
}
