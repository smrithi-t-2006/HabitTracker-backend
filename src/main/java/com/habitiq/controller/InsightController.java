package com.habitiq.controller;

import com.habitiq.dto.InsightDto;
import com.habitiq.service.InsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;

    @GetMapping
    public ResponseEntity<List<InsightDto.Response>> getInsights() {
        return ResponseEntity.ok(insightService.getInsights());
    }

    @GetMapping("/unread")
    public ResponseEntity<List<InsightDto.Response>> getUnreadInsights() {
        return ResponseEntity.ok(insightService.getUnreadInsights());
    }

    @PostMapping("/generate")
    public ResponseEntity<List<InsightDto.Response>> generateInsights() {
        return ResponseEntity.ok(insightService.generateInsights());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        insightService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        insightService.markAllAsRead();
        return ResponseEntity.ok().build();
    }
}
