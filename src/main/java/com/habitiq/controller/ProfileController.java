package com.habitiq.controller;

import com.habitiq.dto.ProfileDto;
import com.habitiq.dto.SettingsDto;
import com.habitiq.entity.User;
import com.habitiq.service.UserService;
import com.habitiq.service.BadgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    @Autowired private UserService userService;
    @Autowired private BadgeService badgeService;

    @GetMapping
    public ResponseEntity<ProfileDto.Response> getProfile() {
        User user = userService.getCurrentUser();
        Integer earnedBadges = badgeService.getUserEarnedBadgeCount();
        return ResponseEntity.ok(new ProfileDto.Response(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getBio(),
                user.getAvatar(),
                user.getTheme(),
                earnedBadges
        ));
    }

    @PutMapping
    public ResponseEntity<ProfileDto.Response> updateProfile(@RequestBody ProfileDto.UpdateRequest request) {
        User user = userService.getCurrentUser();
        userService.updateUserProfile(user, request.getFullName(), request.getBio(), request.getAvatar());
        Integer earnedBadges = badgeService.getUserEarnedBadgeCount();
        return ResponseEntity.ok(new ProfileDto.Response(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getBio(),
                user.getAvatar(),
                user.getTheme(),
                earnedBadges
        ));
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ProfileDto.ChangePasswordRequest request) {
        User user = userService.getCurrentUser();
        userService.changePassword(user, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok("Password changed successfully");
    }

    @GetMapping("/settings")
    public ResponseEntity<SettingsDto.Response> getSettings() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(new SettingsDto.Response(
                user.getTheme(),
                user.getNotificationsEnabled(),
                user.getEmailNotificationsEnabled(),
                user.getStreakNotifications(),
                user.getGoalNotifications()
        ));
    }

    @PutMapping("/settings")
    public ResponseEntity<SettingsDto.Response> updateSettings(@RequestBody SettingsDto.UpdateRequest request) {
        User user = userService.getCurrentUser();
        userService.updateSettings(user,
                request.getTheme(),
                request.getNotificationsEnabled(),
                request.getEmailNotificationsEnabled(),
                request.getStreakNotifications(),
                request.getGoalNotifications()
        );
        return ResponseEntity.ok(new SettingsDto.Response(
                user.getTheme(),
                user.getNotificationsEnabled(),
                user.getEmailNotificationsEnabled(),
                user.getStreakNotifications(),
                user.getGoalNotifications()
        ));
    }
}
