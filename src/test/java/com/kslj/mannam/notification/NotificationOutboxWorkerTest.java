package com.kslj.mannam.notification;

import com.kslj.mannam.domain.notification.entity.NotificationOutbox;
import com.kslj.mannam.domain.notification.enums.OutboxStatus;
import com.kslj.mannam.domain.notification.repository.NotificationOutboxRepository;
import com.kslj.mannam.domain.notification.service.NotificationOutboxWorker;
import com.kslj.mannam.domain.notification.service.NotificationService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.firebase.FcmTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationOutboxWorkerTest {
    @Test
    void deliveryFailureIsRecordedForRetry() {
        NotificationOutboxRepository repository = mock(NotificationOutboxRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        FcmTokenService fcmTokenService = mock(FcmTokenService.class);
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        User user = User.builder().id(1L).build();
        NotificationOutbox event = NotificationOutbox.builder()
                .eventKey("MATCH:10:1").user(user).referenceId(10L).build();
        when(repository.findTop50ByStatusInAndNextAttemptAtBeforeOrderByIdAsc(any(), any(LocalDateTime.class)))
                .thenReturn(List.of(event));
        doThrow(new IllegalStateException("websocket unavailable"))
                .when(messagingTemplate).convertAndSendToUser(eq("1"), anyString(), any(Object.class));
        NotificationOutboxWorker worker = new NotificationOutboxWorker(
                repository, notificationService, fcmTokenService, messagingTemplate);

        worker.publishPending();

        assertThat(event.getStatus()).isEqualTo(OutboxStatus.RETRY);
        assertThat(event.getRetryCount()).isEqualTo(1);
        verify(fcmTokenService, never()).sendPushToUser(any(), anyString(), anyString(), anyString(), any());
    }
}
