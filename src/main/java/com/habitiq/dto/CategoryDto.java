package com.habitiq.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

public class CategoryDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank
        private String name;
        private String description;
        private String icon = "🎯";
        private String color = "#4CAF50";
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class UpdateRequest {
        private String name;
        private String description;
        private String icon;
        private String color;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private String icon;
        private String color;
        private LocalDateTime createdAt;
    }
}
