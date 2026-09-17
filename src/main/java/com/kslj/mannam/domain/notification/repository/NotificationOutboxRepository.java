package com.kslj.mannam.domain.notification.repository;

import com.kslj.mannam.domain.notification.entity.NotificationOutbox;
import com.kslj.mannam.domain.notification.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {
    List<NotificationOutbox> findTop50ByStatusInAndNextAttemptAtBeforeOrderByIdAsc(
            Collection<OutboxStatus> statuses, LocalDateTime now);
}
