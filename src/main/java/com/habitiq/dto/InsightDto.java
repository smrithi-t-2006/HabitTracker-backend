package com.habitiq.dto;

import com.habitiq.entity.Insight;
import lombok.*;
import java.time.LocalDateTime;

public class InsightDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private Insight.InsightType type;
        private String message;
        private LocalDateTime generatedAt;
        private Boolean isRead;
    }
}
