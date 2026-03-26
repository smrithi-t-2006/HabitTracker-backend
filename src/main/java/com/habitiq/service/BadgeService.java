package com.habitiq.service;

import com.habitiq.dto.BadgeDto;
import com.habitiq.dto.BadgeDto.CreateRequest;
import com.habitiq.dto.BadgeDto.UpdateRequest;
import com.habitiq.dto.UserBadgeDto;
import com.habitiq.entity.Badge;
import com.habitiq.entity.Badge.BadgeCategory;
import com.habitiq.entity.User;
import com.habitiq.entity.UserBadge;
import com.habitiq.repository.BadgeRepository;
import com.habitiq.repository.UserBadgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BadgeService {
    @Autowired private BadgeRepository badgeRepository;
    @Autowired private UserBadgeRepository userBadgeRepository;
    @Autowired private UserService userService;

    public List<BadgeDto.Response> getAllBadges() {
        return badgeRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<BadgeDto.Response> getBadgesByCategory(BadgeCategory category) {
        return badgeRepository.findByCategory(category).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BadgeDto.Response createBadge(CreateRequest request) {
        Badge badge = new Badge();
        badge.setName(request.getName());
        badge.setDescription(request.getDescription());
        badge.setIcon(request.getIcon());
        badge.setColor(request.getColor());
        badge.setCategory(request.getCategory());
        badge.setRequiredCount(request.getRequiredCount());
        return toResponse(badgeRepository.save(badge));
    }

    public BadgeDto.Response updateBadge(Long id, UpdateRequest request) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Badge not found"));
        badge.setName(request.getName());
        badge.setDescription(request.getDescription());
        badge.setIcon(request.getIcon());
        badge.setColor(request.getColor());
        badge.setCategory(request.getCategory());
        badge.setRequiredCount(request.getRequiredCount());
        badge.setIsActive(request.getIsActive());
        return toResponse(badgeRepository.save(badge));
    }

    public void deleteBadge(Long id) {
        badgeRepository.deleteById(id);
    }

    public List<UserBadgeDto.Response> getUserBadges() {
        User user = userService.getCurrentUser();
        return userBadgeRepository.findByUser(user).stream()
                .map(this::userBadgeToResponse)
                .collect(Collectors.toList());
    }

    public List<UserBadgeDto.Response> getUserEarnedBadges() {
        User user = userService.getCurrentUser();
        return userBadgeRepository.findByUserAndIsEarnedTrue(user).stream()
                .map(this::userBadgeToResponse)
                .collect(Collectors.toList());
    }

    public void awardBadge(User user, Badge badge) {
        UserBadge userBadge = userBadgeRepository.findByUserIdAndBadgeId(user.getId(), badge.getId())
                .orElse(new UserBadge(null, user, badge, 0, false, null, LocalDateTime.now(), LocalDateTime.now(), null));
        
        userBadge.setIsEarned(true);
        userBadge.setEarnedAt(LocalDateTime.now());
        userBadge.setProgress(badge.getRequiredCount());
        userBadge.setUpdatedAt(LocalDateTime.now());
        userBadgeRepository.save(userBadge);
    }

    public void updateBadgeProgress(User user, Badge badge, Integer progress) {
        UserBadge userBadge = userBadgeRepository.findByUserIdAndBadgeId(user.getId(), badge.getId())
                .orElse(new UserBadge(null, user, badge, 0, false, null, LocalDateTime.now(), LocalDateTime.now(), null));
        
        userBadge.setProgress(Math.min(progress, badge.getRequiredCount()));
        if (progress >= badge.getRequiredCount() && !userBadge.getIsEarned()) {
            userBadge.setIsEarned(true);
            userBadge.setEarnedAt(LocalDateTime.now());
        }
        userBadge.setUpdatedAt(LocalDateTime.now());
        userBadgeRepository.save(userBadge);
    }

    public Integer getUserEarnedBadgeCount() {
        User user = userService.getCurrentUser();
        return userBadgeRepository.countByUserAndIsEarnedTrue(user);
    }

    private BadgeDto.Response toResponse(Badge badge) {
        return new BadgeDto.Response(
                badge.getId(),
                badge.getName(),
                badge.getDescription(),
                badge.getIcon(),
                badge.getColor(),
                badge.getCategory(),
                badge.getRequiredCount(),
                badge.getIsActive(),
                badge.getCreatedAt()
        );
    }

    private UserBadgeDto.Response userBadgeToResponse(UserBadge userBadge) {
        return new UserBadgeDto.Response(
                userBadge.getId(),
                toResponse(userBadge.getBadge()),
                userBadge.getProgress(),
                userBadge.getIsEarned(),
                userBadge.getEarnedAt(),
                userBadge.getProgressDescription()
        );
    }
}
