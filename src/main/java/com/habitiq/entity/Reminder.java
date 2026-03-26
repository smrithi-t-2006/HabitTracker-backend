package com.habitiq.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "reminders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habit_id", nullable = false, unique = true)
    private Habit habit;

    @Column(name = "reminder_time")
    private LocalTime reminderTime;

    @Column(length = 255)
    private String message;

    @Column(name = "is_enabled")
    @Builder.Default
    private Boolean isEnabled = true;
}
