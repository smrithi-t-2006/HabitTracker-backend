package com.habitiq.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Habit> habits = new HashSet<>();

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(columnDefinition = "LONGTEXT")
    private String avatar;

    @Column(nullable = false)
    @Builder.Default
    private String theme = "light";

    @Column(nullable = false)
    @Builder.Default
    private Boolean notificationsEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean emailNotificationsEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean streakNotifications = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean goalNotifications = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
