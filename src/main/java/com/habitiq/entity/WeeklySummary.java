package com.habitiq.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "weekly_summaries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WeeklySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(name = "avg_score")
    @Builder.Default
    private Double avgScore = 0.0;

    @Column(length = 20)
    private String trend; // IMPROVING, DECLINING, STABLE

    @Column(name = "best_day", length = 15)
    private String bestDay;

    @Column(name = "worst_day", length = 15)
    private String worstDay;

    @Column(name = "total_completions")
    @Builder.Default
    private Integer totalCompletions = 0;

    @Column(name = "total_possible")
    @Builder.Default
    private Integer totalPossible = 0;
}
