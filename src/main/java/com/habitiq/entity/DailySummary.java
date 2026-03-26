package com.habitiq.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "daily_summaries", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "summary_date"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    @Column(name = "productivity_score")
    @Builder.Default
    private Double productivityScore = 0.0;

    @Column(name = "total_habits")
    @Builder.Default
    private Integer totalHabits = 0;

    @Column(name = "completed_habits")
    @Builder.Default
    private Integer completedHabits = 0;
}
