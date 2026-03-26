package com.habitiq.dto;

import com.habitiq.entity.Badge.BadgeCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
public class BadgeDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private String name;
        private String description;
        private String icon;
        private String color;
        private BadgeCategory category;
        private Integer requiredCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String name;
        private String description;
        private String icon;
        private String color;
        private BadgeCategory category;
        private Integer requiredCount;
        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private String icon;
        private String color;
        private BadgeCategory category;
        private Integer requiredCount;
        private Boolean isActive;
        private LocalDateTime createdAt;
    }
}

