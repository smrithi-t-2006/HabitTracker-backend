package com.habitiq.repository;

import com.habitiq.entity.Badge;
import com.habitiq.entity.Badge.BadgeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    Optional<Badge> findByName(String name);
    List<Badge> findByCategory(BadgeCategory category);
    List<Badge> findByIsActiveTrue();
}
