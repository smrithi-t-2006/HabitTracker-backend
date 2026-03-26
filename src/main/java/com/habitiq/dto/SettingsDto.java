package com.habitiq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class SettingsDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String theme;
        private Boolean notificationsEnabled;
        private Boolean emailNotificationsEnabled;
        private Boolean streakNotifications;
        private Boolean goalNotifications;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String theme;
        private Boolean notificationsEnabled;
        private Boolean emailNotificationsEnabled;
        private Boolean streakNotifications;
        private Boolean goalNotifications;
    }
}
