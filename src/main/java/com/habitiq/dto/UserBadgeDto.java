package com.habitiq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserBadgeDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private BadgeDto.Response badge;
        private Integer progress;
        private Boolean isEarned;
        private LocalDateTime earnedAt;
        private String progressDescription;
    }
}
