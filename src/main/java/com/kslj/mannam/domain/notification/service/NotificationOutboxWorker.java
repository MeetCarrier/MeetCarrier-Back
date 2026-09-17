package com.kslj.mannam.domain.notification.service;

import com.kslj.mannam.domain.notification.entity.NotificationOutbox;
import com.kslj.mannam.domain.notification.enums.OutboxStatus;
import com.kslj.mannam.domain.notification.repository.NotificationOutboxRepository;
import com.kslj.mannam.firebase.FcmTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationOutboxWorker {
    private final NotificationOutboxRepository outboxRepository;
    private final NotificationService notificationService;
    private final FcmTokenService fcmTokenService;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedDelayString = "${notification.outbox.fixed-delay-ms:10000}")
    @Transactional
    public void publishPending() {
        List<NotificationOutbox> events = outboxRepository
                .findTop50ByStatusInAndNextAttemptAtBeforeOrderByIdAsc(
                        List.of(OutboxStatus.PENDING, OutboxStatus.RETRY), LocalDateTime.now());
        for (NotificationOutbox event : events) {
            try {
                notificationService.saveMatchNotificationIfAbsent(event.getUser(), event.getReferenceId());
                messagingTemplate.convertAndSendToUser(String.valueOf(event.getUser().getId()),
                        "/queue/match-notification", Map.of("matchId", event.getReferenceId()));
                fcmTokenService.sendPushToUser(event.getUser(), "매칭 성사",
                        "매칭이 성사되었어요! 매칭 목록에서 확인해보세요!",
                        "https://www.mannamdeliveries.link/ChatList", null);
                event.markSent();
            } catch (Exception e) {
                event.markFailure(e);
                log.warn("Match notification outbox delivery failed. eventKey={}", event.getEventKey(), e);
            }
        }
    }
}
