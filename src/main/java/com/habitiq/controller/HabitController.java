package com.habitiq.controller;

import com.habitiq.dto.HabitDto;
import com.habitiq.service.HabitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;

    @PostMapping
    public ResponseEntity<HabitDto.Response> createHabit(@Valid @RequestBody HabitDto.CreateRequest request) {
        return ResponseEntity.ok(habitService.createHabit(request));
    }

    @GetMapping
    public ResponseEntity<List<HabitDto.Response>> getHabits() {
        return ResponseEntity.ok(habitService.getUserHabits());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HabitDto.Response> getHabit(@PathVariable Long id) {
        return ResponseEntity.ok(habitService.getHabit(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HabitDto.Response> updateHabit(@PathVariable Long id,
                                                          @RequestBody HabitDto.UpdateRequest request) {
        return ResponseEntity.ok(habitService.updateHabit(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long id) {
        habitService.deleteHabit(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/log")
    public ResponseEntity<HabitDto.LogResponse> logHabit(@PathVariable Long id,
                                                          @RequestBody HabitDto.LogRequest request) {
        return ResponseEntity.ok(habitService.logHabit(id, request));
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<List<HabitDto.LogResponse>> getHabitLogs(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(habitService.getHabitLogs(id, start, end));
    }

    @GetMapping("/today")
    public ResponseEntity<List<HabitDto.LogResponse>> getTodayLogs() {
        return ResponseEntity.ok(habitService.getTodayLogs());
    }

    @PostMapping("/{id}/reminder")
    public ResponseEntity<HabitDto.ReminderInfo> setReminder(@PathVariable Long id,
                                                              @RequestBody HabitDto.ReminderRequest request) {
        return ResponseEntity.ok(habitService.setReminder(id, request));
    }
}
