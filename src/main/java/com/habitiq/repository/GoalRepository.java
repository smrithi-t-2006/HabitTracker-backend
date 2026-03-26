package com.habitiq.repository;

import com.habitiq.entity.Goal;
import com.habitiq.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByHabit(Habit habit);
    
    @Query("SELECT g FROM Goal g WHERE g.habit = :habit AND g.status = 'IN_PROGRESS'")
    List<Goal> findActiveGoalsByHabit(@Param("habit") Habit habit);
    
    @Query("SELECT g FROM Goal g WHERE g.habit.user.id = :userId AND g.isCompleted = false")
    List<Goal> findIncompleteGoalsByUserId(@Param("userId") Long userId);
}
