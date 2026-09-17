package com.kslj.mannam.domain.notification.entity;

import com.kslj.mannam.domain.notification.enums.OutboxStatus;
import com.kslj.mannam.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification_outbox", uniqueConstraints = @UniqueConstraint(name = "uk_notification_outbox_event", columnNames = "event_key"))
public class NotificationOutbox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_key", nullable = false, length = 150)
    private String eventKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status = OutboxStatus.PENDING;

    @Builder.Default
    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Builder.Default
    @Column(name = "next_attempt_at", nullable = false)
    private LocalDateTime nextAttemptAt = LocalDateTime.now();

    @Column(name = "last_error", length = 1000)
    private String lastError;

    public void markSent() {
        status = OutboxStatus.SENT;
        lastError = null;
    }

    public void markFailure(Throwable error) {
        retryCount++;
        lastError = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
        if (retryCount >= 5) {
            status = OutboxStatus.FAILED;
            return;
        }
        status = OutboxStatus.RETRY;
        long delaySeconds = Math.min(300, 10L * (1L << (retryCount - 1)));
        nextAttemptAt = LocalDateTime.now().plusSeconds(delaySeconds);
    }
}
