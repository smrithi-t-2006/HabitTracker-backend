package com.habitiq.service;

import com.habitiq.entity.User;
import com.habitiq.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        throw new RuntimeException("User not authenticated");
    }

    public void updateUserProfile(User user, String fullName, String bio, String avatar) {
        user.setFullName(fullName);
        user.setBio(bio);
        if (avatar != null && !avatar.isEmpty()) {
            user.setAvatar(avatar);
        }
        userRepository.save(user);
    }

    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void updateSettings(User user, String theme, Boolean notificationsEnabled, Boolean emailNotificationsEnabled,
                              Boolean streakNotifications, Boolean goalNotifications) {
        if (theme != null) {
            user.setTheme(theme);
        }
        if (notificationsEnabled != null) {
            user.setNotificationsEnabled(notificationsEnabled);
        }
        if (emailNotificationsEnabled != null) {
            user.setEmailNotificationsEnabled(emailNotificationsEnabled);
        }
        if (streakNotifications != null) {
            user.setStreakNotifications(streakNotifications);
        }
        if (goalNotifications != null) {
            user.setGoalNotifications(goalNotifications);
        }
        userRepository.save(user);
    }
}
