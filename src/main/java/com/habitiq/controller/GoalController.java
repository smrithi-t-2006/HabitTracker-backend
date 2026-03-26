package com.habitiq.controller;

import com.habitiq.dto.GoalDto;
import com.habitiq.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    public ResponseEntity<GoalDto.Response> createGoal(@Valid @RequestBody GoalDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.createGoal(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalDto.Response> getGoalById(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.getGoalById(id));
    }

    @GetMapping("/habit/{habitId}")
    public ResponseEntity<List<GoalDto.Response>> getGoalsByHabit(@PathVariable Long habitId) {
        return ResponseEntity.ok(goalService.getGoalsByHabit(habitId));
    }

    @GetMapping("/active/list")
    public ResponseEntity<List<GoalDto.Response>> getActiveGoals() {
        return ResponseEntity.ok(goalService.getActiveGoals());
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalDto.Response> updateGoal(@PathVariable Long id,
                                                        @Valid @RequestBody GoalDto.UpdateRequest request) {
        return ResponseEntity.ok(goalService.updateGoal(id, request));
    }

    @PutMapping("/{id}/progress")
    public ResponseEntity<GoalDto.Response> updateProgress(@PathVariable Long id,
                                                            @Valid @RequestBody GoalDto.ProgressUpdate request) {
        return ResponseEntity.ok(goalService.updateProgress(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        goalService.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }
}
