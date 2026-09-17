package com.kslj.mannam.match;

import com.kslj.mannam.domain.match.dto.MatchFinalizationResult;
import com.kslj.mannam.domain.match.entity.MatchRequest;
import com.kslj.mannam.domain.match.enums.RequestStatus;
import com.kslj.mannam.domain.match.repository.MatchRequestRepository;
import com.kslj.mannam.domain.match.service.MatchFinalizationService;
import com.kslj.mannam.domain.match.service.MatchOrchestrationService;
import com.kslj.mannam.domain.match.service.MatchRequestService;
import com.kslj.mannam.domain.notification.service.NotificationService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MatchRequestServiceTest {
    @AfterEach
    void cleanupSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void acceptedRequestUsesLockedRowAndCleansRedisAfterCommit() {
        MatchRequestRepository repository = mock(MatchRequestRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        UserService userService = mock(UserService.class);
        MatchFinalizationService finalizationService = mock(MatchFinalizationService.class);
        MatchOrchestrationService orchestrationService = mock(MatchOrchestrationService.class);
        User sender = User.builder().id(1L).build();
        User receiver = User.builder().id(2L).build();
        MatchRequest request = MatchRequest.builder().sender(sender).receiver(receiver).status(RequestStatus.PENDING).build();
        when(repository.findByIdForUpdate(10L)).thenReturn(Optional.of(request));
        when(userService.getUserById(2L)).thenReturn(receiver);
        when(finalizationService.finalizeMatch(2L, 1L, 0.0))
                .thenReturn(new MatchFinalizationResult(true, 100L, 200L));
        MatchRequestService service = new MatchRequestService(repository, notificationService, userService,
                finalizationService, orchestrationService);
        TransactionSynchronizationManager.initSynchronization();

        boolean accepted = service.processRespond(2L, 10L, true);
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        synchronizations.forEach(TransactionSynchronization::afterCommit);

        assertThat(accepted).isTrue();
        assertThat(request.getStatus()).isEqualTo(RequestStatus.ACCEPTED);
        verify(repository).findByIdForUpdate(10L);
        verify(orchestrationService).completeExternalMatch(2L, 1L);
    }

    @Test
    void conflictExpiresRequest() {
        MatchRequestRepository repository = mock(MatchRequestRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        UserService userService = mock(UserService.class);
        MatchFinalizationService finalizationService = mock(MatchFinalizationService.class);
        MatchOrchestrationService orchestrationService = mock(MatchOrchestrationService.class);
        User sender = User.builder().id(1L).build();
        User receiver = User.builder().id(2L).build();
        MatchRequest request = MatchRequest.builder().sender(sender).receiver(receiver).status(RequestStatus.PENDING).build();
        when(repository.findByIdForUpdate(10L)).thenReturn(Optional.of(request));
        when(userService.getUserById(2L)).thenReturn(receiver);
        when(finalizationService.finalizeMatch(2L, 1L, 0.0)).thenReturn(MatchFinalizationResult.conflict());
        MatchRequestService service = new MatchRequestService(repository, notificationService, userService,
                finalizationService, orchestrationService);

        boolean accepted = service.processRespond(2L, 10L, true);

        assertThat(accepted).isFalse();
        assertThat(request.getStatus()).isEqualTo(RequestStatus.EXPIRED);
        verifyNoInteractions(orchestrationService);
    }
}
