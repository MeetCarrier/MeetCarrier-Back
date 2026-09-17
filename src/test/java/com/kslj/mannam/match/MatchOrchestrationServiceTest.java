package com.kslj.mannam.match;

import com.kslj.mannam.domain.block.service.BlockService;
import com.kslj.mannam.domain.match.dto.*;
import com.kslj.mannam.domain.match.enums.MatchRequestState;
import com.kslj.mannam.domain.match.repository.MatchRepository;
import com.kslj.mannam.domain.match.service.*;
import com.kslj.mannam.domain.review.service.ReviewService;
import com.kslj.mannam.domain.test.dto.TestResponseDto;
import com.kslj.mannam.domain.test.service.TestService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.enums.Gender;
import com.kslj.mannam.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchOrchestrationServiceTest {
    @Mock MatchQueueStore queueStore;
    @Mock MatchScoringGateway scoringGateway;
    @Mock MatchFinalizationService finalizationService;
    @Mock MatchService matchService;
    @Mock UserService userService;
    @Mock TestService testService;
    @Mock ReviewService reviewService;
    @Mock BlockService blockService;
    @Mock MatchRepository matchRepository;
    @Mock RabbitTemplate rabbitTemplate;
    @Mock SimpMessagingTemplate messagingTemplate;

    MatchOrchestrationService service;

    @BeforeEach
    void setUp() {
        service = new MatchOrchestrationService(queueStore, scoringGateway, finalizationService, matchService,
                userService, testService, reviewService, blockService, matchRepository, rabbitTemplate, messagingTemplate);
    }

    @Test
    void duplicateRequestPublishesOnlyOneAdmissionMessage() {
        User user = user(1L);
        UUID requestId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        when(queueStore.register(eq(1L), any(UUID.class), any(LocalDateTime.class)))
                .thenReturn(state(requestId, 1L, MatchRequestState.QUEUED, false, now))
                .thenReturn(state(requestId, 1L, MatchRequestState.QUEUED, true, now));

        MatchRequestStateDto first = service.addNewUser(user);
        MatchRequestStateDto second = service.addNewUser(user);

        assertThat(first.getRequestId()).isEqualTo(second.getRequestId());
        verify(rabbitTemplate, times(1)).convertAndSend(eq("match_admission_queue"), any(MatchAdmissionRequestDto.class));
    }

    @Test
    void nextAdmissionSeesPreviousLowScoreUserAsCandidate() {
        User b = user(2L);
        User c = user(3L);
        UUID bRequest = UUID.randomUUID();
        UUID cRequest = UUID.randomUUID();
        LocalDateTime bTime = LocalDateTime.now().minusSeconds(1);
        LocalDateTime cTime = LocalDateTime.now();

        when(userService.getUserById(2L)).thenReturn(b);
        when(userService.getUserById(3L)).thenReturn(c);
        when(matchService.canRequestNewMatch(any())).thenReturn(true);
        when(queueStore.startProcessing(anyLong(), any())).thenReturn(true);
        when(testService.getTestByUserId(any())).thenReturn(List.of(TestResponseDto.builder()
                .depressionScore(1).efficacyScore(2).relationshipScore(3).build()));
        when(reviewService.getReviewByReviewerId(anyLong())).thenReturn(List.of());
        when(blockService.getBlocks(any())).thenReturn(List.of());
        when(matchRepository.findAllByUser1OrUser2(any(), any())).thenReturn(List.of());
        when(scoringGateway.score(any())).thenReturn(MatchFilterResponseDto.builder().filterResults(List.of()).build());

        when(queueStore.getState(2L))
                .thenReturn(Optional.of(state(bRequest, 2L, MatchRequestState.QUEUED, false, bTime)))
                .thenReturn(Optional.of(state(bRequest, 2L, MatchRequestState.PROCESSING, false, bTime)));
        when(queueStore.getWaitingEntries()).thenReturn(List.of());
        service.processAdmission(admission(bRequest, 2L, bTime));

        ArgumentCaptor<MatchingQueueEntry> waitingCaptor = ArgumentCaptor.forClass(MatchingQueueEntry.class);
        verify(queueStore).addWaiting(waitingCaptor.capture(), eq(bRequest));
        MatchingQueueEntry bWaiting = waitingCaptor.getValue();

        when(queueStore.getState(3L))
                .thenReturn(Optional.of(state(cRequest, 3L, MatchRequestState.QUEUED, false, cTime)))
                .thenReturn(Optional.of(state(cRequest, 3L, MatchRequestState.PROCESSING, false, cTime)));
        when(queueStore.getWaitingEntries()).thenReturn(List.of(bWaiting));
        service.processAdmission(admission(cRequest, 3L, cTime));

        ArgumentCaptor<MatchFilterRequestDto> requestCaptor = ArgumentCaptor.forClass(MatchFilterRequestDto.class);
        verify(scoringGateway, times(2)).score(requestCaptor.capture());
        MatchFilterRequestDto cScoringRequest = requestCaptor.getAllValues().get(1);
        assertThat(cScoringRequest.getWaitingUsers()).extracting(MatchQueueRequestDto::getUserId).containsExactly(2L);
    }

    private User user(long id) {
        return User.builder().id(id).socialId("social-" + id).nickname("user-" + id).gender(Gender.Male)
                .age(25L).latitude(37.0).longitude(127.0).phone("010-0000-000" + id).build();
    }

    private MatchAdmissionRequestDto admission(UUID id, long userId, LocalDateTime time) {
        return MatchAdmissionRequestDto.builder().requestId(id).userId(userId).requestedAt(time).build();
    }

    private MatchRequestStateDto state(UUID id, long userId, MatchRequestState status, boolean duplicate, LocalDateTime time) {
        return MatchRequestStateDto.builder().requestId(id).userId(userId).status(status)
                .requestedAt(time).duplicate(duplicate).build();
    }
}
