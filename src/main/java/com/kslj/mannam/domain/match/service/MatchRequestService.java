package com.kslj.mannam.domain.match.service;

import com.kslj.mannam.domain.match.dto.MatchCreateDto;
import com.kslj.mannam.domain.match.entity.MatchRequest;
import com.kslj.mannam.domain.match.enums.RequestStatus;
import com.kslj.mannam.domain.match.repository.MatchRequestRepository;
import com.kslj.mannam.domain.notification.enums.NotificationType;
import com.kslj.mannam.domain.notification.service.NotificationService;
import com.kslj.mannam.domain.survey.service.SurveyService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchRequestService {

    private final MatchRequestRepository matchRequestRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final MatchService matchService;
    private final SurveyService surveyService;
    private final MatchQueueManager matchQueueManager;

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
        MatchRequest request = matchRequestRepository.findById(requestId).orElseThrow();

        if (request.getReceiver().getId() != receiverId) {
            throw new IllegalStateException("해당 유저에게 전달된 매칭 요청이 아닙니다.");
        }
        log.info("receiverId={}, requestId={}, isAccepted={}", receiverId, requestId, isAccepted);

        if (request.getStatus() != RequestStatus.PENDING) {
            return false;
        }

        // 거절 처리
        if (!isAccepted) {
            request.updateStatus(RequestStatus.REJECTED);
            return false;
        }

        // 수락 처리
        request.updateStatus(RequestStatus.ACCEPTED);
        matchQueueManager.cancelMatching(receiverId);

        User receiver = userService.getUserById(receiverId);
        User sender = request.getSender();

        // 매칭 데이터 생성
        long matchId = matchService.createMatch(MatchCreateDto.builder()
                .user1Id(receiver.getId())
                .user2Id(sender.getId())
                .build()
        );

        // 설문지 생성
        long sessionId = surveyService.createSurveySession(matchId);
        surveyService.createSurveyQuestions(matchId, sessionId);

        // 알림 등록
        notificationService.createNotification(NotificationType.Match, receiver, matchId);
        notificationService.createNotification(NotificationType.Match, sender, matchId);

        return true;
    }
}
