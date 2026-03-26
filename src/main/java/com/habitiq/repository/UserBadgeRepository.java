package com.habitiq.repository;

import com.habitiq.entity.UserBadge;
import com.habitiq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUser(User user);
    List<UserBadge> findByUserAndIsEarnedTrue(User user);
    Optional<UserBadge> findByUserIdAndBadgeId(Long userId, Long badgeId);
    Integer countByUserAndIsEarnedTrue(User user);
}
