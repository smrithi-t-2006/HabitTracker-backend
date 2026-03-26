package com.habitiq.dto;

import com.habitiq.entity.JournalEntry.Mood;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class JournalEntryDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private Long habitId;
        private String title;
        private String content;
        private Mood mood;
        private LocalDate entryDate;
        private Integer duration;
        private String tags;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String title;
        private String content;
        private Mood mood;
        private Integer duration;
        private String tags;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long habitId;
        private String habitName;
        private String title;
        private String content;
        private Mood mood;
        private LocalDate entryDate;
        private Integer duration;
        private String tags;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
