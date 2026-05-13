package com.nexus.server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column(nullable = false)
    private Double tokensUsed;

    @Column(nullable = false)
    private Double cost;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        completedAt = LocalDateTime.now();
    }
}

enum RequestStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}
