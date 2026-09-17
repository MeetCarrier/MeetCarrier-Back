package com.kslj.mannam.domain.match.service;

import com.kslj.mannam.domain.match.dto.MatchFinalizationResult;
import com.kslj.mannam.domain.match.entity.MatchRequest;
import com.kslj.mannam.domain.match.enums.RequestStatus;
import com.kslj.mannam.domain.match.repository.MatchRequestRepository;
import com.kslj.mannam.domain.notification.enums.NotificationType;
import com.kslj.mannam.domain.notification.service.NotificationService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchRequestService {

    private final MatchRequestRepository matchRequestRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final MatchFinalizationService finalizationService;
    private final MatchOrchestrationService matchOrchestrationService;

    @Transactional
    public void createMatchRequest(long senderId, long receiverId) {
        User sender = userService.getUserById(senderId);
        User receiver = userService.getUserById(receiverId);

        // 매칭 요청 DB에 저장
        MatchRequest matchRequest = MatchRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(RequestStatus.PENDING)
                .build();
        MatchRequest savedRequest = matchRequestRepository.save(matchRequest);

        // 수신자의 알람 센터에 알람 추가
        notificationService.createNotification(NotificationType.Request, receiver, savedRequest.getId());
    }

    @Transactional
    public boolean processRespond(long receiverId, long requestId, boolean isAccepted) {
        MatchRequest request = matchRequestRepository.findByIdForUpdate(requestId).orElseThrow();

        if (request.getReceiver().getId() != receiverId) {
            throw new IllegalStateException("해당 유저에게 전달된 매칭 요청이 아닙니다.");
        }
        log.info("receiverId={}, requestId={}, isAccepted={}", receiverId, requestId, isAccepted);

        if (request.getStatus() != RequestStatus.PENDING) return request.getStatus() == RequestStatus.ACCEPTED;

        User receiver = userService.getUserById(receiverId);
        User sender = request.getSender();

        // 거절 처리
        if (!isAccepted) {
            request.updateStatus(RequestStatus.REJECTED);
            notificationService.createNotification(NotificationType.MatchRejected, sender, null);
            return false;
        }

        // 수락 처리
        MatchFinalizationResult result = finalizationService.finalizeMatch(receiver.getId(), sender.getId(), 0.0);
        if (!result.isSuccess()) {
            request.updateStatus(RequestStatus.EXPIRED);
            return false;
        }
        request.updateStatus(RequestStatus.ACCEPTED);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                matchOrchestrationService.completeExternalMatch(receiver.getId(), sender.getId());
            }
        });
        return true;
    }
}
