package com.habitiq.service;

import com.habitiq.dto.GoalDto;
import com.habitiq.entity.Goal;
import com.habitiq.entity.Habit;
import com.habitiq.entity.User;
import com.habitiq.exception.BadRequestException;
import com.habitiq.exception.ResourceNotFoundException;
import com.habitiq.repository.GoalRepository;
import com.habitiq.repository.HabitRepository;
import com.habitiq.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final HabitRepository habitRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public GoalDto.Response createGoal(GoalDto.CreateRequest request) {
        Habit habit = habitRepository.findById(request.getHabitId())
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found"));

        // Verify that the habit belongs to the current user
        User currentUser = getCurrentUser();
        if (!habit.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You don't have permission to create goals for this habit");
        }

        if (request.getTargetValue() <= 0) {
            throw new BadRequestException("Target value must be greater than 0");
        }

        Goal goal = Goal.builder()
                .habit(habit)
                .title(request.getTitle())
                .description(request.getDescription())
                .targetValue(request.getTargetValue())
                .unit(request.getUnit())
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDateTime.now())
                .dueDate(request.getDueDate())
                .currentProgress(0.0)
                .status(Goal.GoalStatus.IN_PROGRESS)
                .isCompleted(false)
                .build();

        goal = goalRepository.save(goal);
        return mapToResponse(goal);
    }

    @Transactional
    public GoalDto.Response updateGoal(Long id, GoalDto.UpdateRequest request) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        User currentUser = getCurrentUser();
        if (!goal.getHabit().getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You don't have permission to update this goal");
        }

        if (request.getTitle() != null) {
            goal.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            goal.setDescription(request.getDescription());
        }
        if (request.getTargetValue() != null) {
            if (request.getTargetValue() <= 0) {
                throw new BadRequestException("Target value must be greater than 0");
            }
            goal.setTargetValue(request.getTargetValue());
        }
        if (request.getUnit() != null) {
            goal.setUnit(request.getUnit());
        }
        if (request.getCurrentProgress() != null) {
            goal.setCurrentProgress(request.getCurrentProgress());
            if (request.getCurrentProgress() >= goal.getTargetValue()) {
                goal.setIsCompleted(true);
                goal.setStatus(Goal.GoalStatus.COMPLETED);
            }
        }
        if (request.getDueDate() != null) {
            goal.setDueDate(request.getDueDate());
        }
        if (request.getStatus() != null) {
            goal.setStatus(request.getStatus());
        }

        goal.setUpdatedAt(LocalDateTime.now());
        goal = goalRepository.save(goal);
        return mapToResponse(goal);
    }

    @Transactional
    public GoalDto.Response updateProgress(Long id, GoalDto.ProgressUpdate request) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        User currentUser = getCurrentUser();
        if (!goal.getHabit().getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You don't have permission to update this goal");
        }

        if (request.getProgressValue() < 0) {
            throw new BadRequestException("Progress value cannot be negative");
        }

        goal.setCurrentProgress(request.getProgressValue());
        
        if (request.getProgressValue() >= goal.getTargetValue()) {
            goal.setIsCompleted(true);
            goal.setStatus(Goal.GoalStatus.COMPLETED);
        }

        goal.setUpdatedAt(LocalDateTime.now());
        goal = goalRepository.save(goal);
        return mapToResponse(goal);
    }

    public GoalDto.Response getGoalById(Long id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        
        User currentUser = getCurrentUser();
        if (!goal.getHabit().getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You don't have permission to view this goal");
        }

        return mapToResponse(goal);
    }

    public List<GoalDto.Response> getGoalsByHabit(Long habitId) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found"));
        
        User currentUser = getCurrentUser();
        if (!habit.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You don't have permission to view goals for this habit");
        }

        return goalRepository.findByHabit(habit).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<GoalDto.Response> getActiveGoals() {
        User currentUser = getCurrentUser();
        return goalRepository.findIncompleteGoalsByUserId(currentUser.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteGoal(Long id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        
        User currentUser = getCurrentUser();
        if (!goal.getHabit().getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You don't have permission to delete this goal");
        }

        goalRepository.delete(goal);
    }

    private GoalDto.Response mapToResponse(Goal goal) {
        double progressPercentage = goal.getTargetValue() > 0 
            ? (goal.getCurrentProgress() / goal.getTargetValue()) * 100 
            : 0;

        return new GoalDto.Response(
                goal.getId(),
                goal.getHabit().getId(),
                goal.getHabit().getName(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getTargetValue(),
                goal.getUnit(),
                goal.getCurrentProgress(),
                progressPercentage,
                goal.getStartDate(),
                goal.getDueDate(),
                goal.getStatus(),
                goal.getIsCompleted(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }
}
