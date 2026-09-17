package com.kslj.mannam.domain.match.service;

import com.kslj.mannam.domain.block.dto.BlockResponseDto;
import com.kslj.mannam.domain.block.service.BlockService;
import com.kslj.mannam.domain.match.dto.*;
import com.kslj.mannam.domain.match.enums.MatchRequestState;
import com.kslj.mannam.domain.match.repository.MatchRepository;
import com.kslj.mannam.domain.review.dto.ReviewQueueDto;
import com.kslj.mannam.domain.review.service.ReviewService;
import com.kslj.mannam.domain.test.dto.TestResponseDto;
import com.kslj.mannam.domain.test.service.TestService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchOrchestrationService {
    private static final double MATCH_THRESHOLD = 0.6;
    private static final long TIMEOUT_SECONDS = 300;

    private final MatchQueueStore queueStore;
    private final MatchScoringGateway scoringGateway;
    private final MatchFinalizationService finalizationService;
    private final MatchService matchService;
    private final UserService userService;
    private final TestService testService;
    private final ReviewService reviewService;
    private final BlockService blockService;
    private final MatchRepository matchRepository;
    private final RabbitTemplate rabbitTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    public MatchRequestStateDto addNewUser(User user) {
        UUID requestId = UUID.randomUUID();
        MatchRequestStateDto state = queueStore.register(user.getId(), requestId, LocalDateTime.now());
        sendStatus(state);
        if (!state.isDuplicate()) {
            rabbitTemplate.convertAndSend("match_admission_queue", MatchAdmissionRequestDto.builder()
                    .requestId(requestId).userId(user.getId()).requestedAt(state.getRequestedAt()).build());
        }
        return state;
    }

    @RabbitListener(queues = "match_admission_queue", containerFactory = "matchAdmissionListenerFactory")
    public void processAdmission(MatchAdmissionRequestDto admission) {
        MatchRequestStateDto current = queueStore.getState(admission.getUserId()).orElse(null);
        if (!isCurrentActive(current, admission)) return;

        User requester = userService.getUserById(admission.getUserId());
        if (!matchService.canRequestNewMatch(requester)) {
            queueStore.markMatched(requester.getId());
            return;
        }
        if (current.getStatus() == MatchRequestState.RESERVED &&
                !queueStore.recoverReservation(requester.getId(), admission.getRequestId())) {
            throw new IllegalStateException("예약된 매칭 상태를 복구할 수 없습니다. requestId=" + admission.getRequestId());
        }
        if (!queueStore.startProcessing(requester.getId(), admission.getRequestId())) return;

        MatchQueueRequestDto requesterData = buildUserData(requester);
        List<MatchingQueueEntry> candidates = filterCandidates(requester, requesterData, queueStore.getWaitingEntries());
        MatchFilterResponseDto response;
        try {
            response = scoringGateway.score(MatchFilterRequestDto.builder()
                    .requestId(admission.getRequestId())
                    .waitingUsers(candidates.stream().map(MatchingQueueEntry::getUserData).toList())
                    .newUser(requesterData).build());
        } catch (RuntimeException e) {
            queueStore.transitionState(requester.getId(), admission.getRequestId(),
                    MatchRequestState.PROCESSING, MatchRequestState.FAILED);
            sendStatus(status(admission, MatchRequestState.FAILED, "매칭 점수 계산에 실패했습니다."));
            return;
        }

        current = queueStore.getState(requester.getId()).orElse(null);
        if (current == null || !current.getRequestId().equals(admission.getRequestId()) ||
                current.getStatus() != MatchRequestState.PROCESSING) return;

        List<FilterResultDto> results = response.getFilterResults() == null ? List.of() : response.getFilterResults();
        queueStore.updateScoresBidirectionally(requester.getId(), results);
        List<FilterResultDto> acceptable = results.stream()
                .filter(result -> result.getUserId() != requester.getId())
                .filter(result -> result.getFinalScore() >= MATCH_THRESHOLD)
                .sorted(Comparator.comparingDouble(FilterResultDto::getFinalScore).reversed()).toList();

        for (FilterResultDto candidate : acceptable) {
            if (!queueStore.reserve(requester.getId(), admission.getRequestId(), candidate.getUserId())) continue;
            MatchFinalizationResult finalized = finalizationService.finalizeMatch(
                    requester.getId(), candidate.getUserId(), candidate.getFinalScore());
            if (finalized.isSuccess()) {
                UUID candidateRequestId = queueStore.getState(candidate.getUserId())
                        .map(MatchRequestStateDto::getRequestId).orElse(null);
                queueStore.markMatched(requester.getId());
                queueStore.markMatched(candidate.getUserId());
                sendMatchSuccess(admission.getRequestId(), requester.getId(), candidate.getUserId(),
                        candidate.getFinalScore(), finalized.getSurveySessionId());
                sendMatchSuccess(candidateRequestId, candidate.getUserId(), requester.getId(),
                        candidate.getFinalScore(), finalized.getSurveySessionId());
                return;
            }
            queueStore.restoreWaiting(candidate.getUserId());
            if (!matchService.canRequestNewMatch(requester)) {
                queueStore.markMatched(requester.getId());
                return;
            }
            queueStore.restoreProcessing(requester.getId(), admission.getRequestId());
        }

        queueStore.addWaiting(MatchingQueueEntry.builder().userData(requesterData)
                .joinTime(admission.getRequestedAt()).scoreMap(queueStore.getScores(requester.getId())).build(),
                admission.getRequestId());
        sendStatus(status(admission, MatchRequestState.WAITING, "적합한 상대를 기다리고 있습니다."));
    }

    public boolean cancelMatching(long userId) {
        boolean cancelled = queueStore.cancel(userId);
        if (cancelled) sendStatus(queueStore.getState(userId).orElseThrow());
        return cancelled;
    }

    public boolean isUserInQueue(long userId) {
        return queueStore.isActive(userId);
    }

    public void completeExternalMatch(long user1Id, long user2Id) {
        try {
            queueStore.markMatched(user1Id);
            queueStore.markMatched(user2Id);
        } catch (RuntimeException e) {
            log.warn("DB match committed but Redis cleanup failed. users={},{}", user1Id, user2Id, e);
        }
    }

    public void addWaitingUserDirectly(MatchQueueRequestDto userData) {
        queueStore.addWaitingDirectly(userData);
    }

    public List<WaitingUserInfoDto> getWaitingUsers() {
        return queueStore.getWaitingEntries().stream().map(entry -> WaitingUserInfoDto.builder()
                .userData(entry.getUserData()).scoreMap(queueStore.getScores(entry.getUserData().getUserId())).build()).toList();
    }

    @Scheduled(fixedDelay = 5000)
    public void checkTimeouts() {
        for (long userId : queueStore.findTimedOutUsers(LocalDateTime.now().minusSeconds(TIMEOUT_SECONDS))) {
            MatchRequestStateDto state = queueStore.getState(userId).orElse(null);
            if (state == null || state.getStatus() != MatchRequestState.WAITING) continue;
            List<Long> recommendations = queueStore.getTopMatches(userId);
            queueStore.timeout(userId);
            messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/match-result",
                    MatchTimeoutDto.builder().requestId(state.getRequestId()).recommendedUserIds(recommendations)
                            .message("매칭 실패. 추천 상대를 안내합니다.").build());
        }
    }

    @Scheduled(fixedDelayString = "${matching.reconcile.fixed-delay-ms:60000}")
    public void reconcileWaitingUsers() {
        for (MatchingQueueEntry entry : queueStore.getWaitingEntries()) {
            try {
                User user = userService.getUserById(entry.getUserData().getUserId());
                if (!matchService.canRequestNewMatch(user)) queueStore.markMatched(user.getId());
            } catch (RuntimeException e) {
                log.warn("Failed to reconcile matching state for userId={}", entry.getUserData().getUserId(), e);
            }
        }
    }

    private MatchQueueRequestDto buildUserData(User user) {
        List<TestResponseDto> tests = testService.getTestByUserId(user);
        if (tests.isEmpty()) throw new IllegalStateException("매칭 전에 자기 평가를 완료해야 합니다.");
        TestResponseDto test = tests.get(0);
        List<ReviewQueueDto> reviews = reviewService.getReviewByReviewerId(user.getId()).stream()
                .map(review -> ReviewQueueDto.builder().rating(review.getRating())
                        .userId(review.getUserId()).step(review.getStep()).build()).toList();
        return MatchQueueRequestDto.builder().userId(user.getId()).latitude(user.getLatitude())
                .longitude(user.getLongitude()).interests(user.getInterests()).gender(user.getGender())
                .age(user.getAge()).phone(user.getPhone()).depressionScore(test.getDepressionScore())
                .efficacyScore(test.getEfficacyScore()).relationshipScore(test.getRelationshipScore())
                .reviews(reviews).build();
    }

    private List<MatchingQueueEntry> filterCandidates(User requester, MatchQueueRequestDto requesterData,
                                                       List<MatchingQueueEntry> entries) {
        Set<String> blockedPhones = blockService.getBlocks(requester).stream()
                .map(BlockResponseDto::getBlockedPhone).collect(Collectors.toSet());
        Set<Long> history = matchRepository.findAllByUser1OrUser2(requester, requester).stream()
                .map(match -> match.getUser1().getId().equals(requester.getId()) ?
                        match.getUser2().getId() : match.getUser1().getId()).collect(Collectors.toSet());
        return entries.stream().filter(entry -> {
            MatchQueueRequestDto candidate = entry.getUserData();
            if (candidate.getUserId() == requester.getId()) return false;
            boolean age = candidate.getAge() >= requesterData.getAge() - requester.getMaxAgeGap() &&
                    candidate.getAge() <= requesterData.getAge() + requester.getMaxAgeGap();
            boolean gender = requester.isAllowOppositeGender() || candidate.getGender() == requester.getGender();
            boolean block = !blockedPhones.contains(candidate.getPhone());
            boolean past = !history.contains(candidate.getUserId());
            boolean distance = getDistance(requesterData.getLatitude(), requesterData.getLongitude(),
                    candidate.getLatitude(), candidate.getLongitude()) <= requester.getMaxMatchingDistance();
            return age && gender && block && past && distance;
        }).toList();
    }

    private boolean isCurrentActive(MatchRequestStateDto state, MatchAdmissionRequestDto admission) {
        return state != null && state.getRequestId().equals(admission.getRequestId()) && state.getStatus().isActive();
    }

    private MatchRequestStateDto status(MatchAdmissionRequestDto admission, MatchRequestState state, String message) {
        return MatchRequestStateDto.builder().requestId(admission.getRequestId()).userId(admission.getUserId())
                .status(state).requestedAt(admission.getRequestedAt()).message(message).build();
    }

    private void sendStatus(MatchRequestStateDto state) {
        messagingTemplate.convertAndSendToUser(String.valueOf(state.getUserId()), "/queue/match-status", state);
    }

    private void sendMatchSuccess(UUID requestId, long receiverId, long matchedUserId, double score, long sessionId) {
        try {
            messagingTemplate.convertAndSendToUser(String.valueOf(receiverId), "/queue/match-result",
                    MatchResultDto.builder().requestId(requestId).matchedUserId(matchedUserId)
                            .finalScore(score).surveySessionId(sessionId).build());
        } catch (RuntimeException e) {
            log.warn("WebSocket match result delivery failed. receiverId={}", receiverId, e);
        }
    }

    public double getDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) return Double.MAX_VALUE;
        double radius = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double value = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return radius * 2 * Math.atan2(Math.sqrt(value), Math.sqrt(1 - value));
    }
}
