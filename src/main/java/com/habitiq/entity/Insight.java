package com.habitiq.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "insights")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Insight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InsightType type;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }

    public enum InsightType {
        TIP, RISK_ALERT, PROGRESS, ADAPTIVE_SUGGESTION
    }
}
