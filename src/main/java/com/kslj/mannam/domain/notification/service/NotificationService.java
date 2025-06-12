package com.kslj.mannam.domain.notification.service;

import com.kslj.mannam.domain.notification.dto.NotificationResponseDto;
import com.kslj.mannam.domain.notification.entity.Notification;
import com.kslj.mannam.domain.notification.enums.NotificationType;
import com.kslj.mannam.domain.notification.repository.NotificationRepository;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.firebase.FcmTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final FcmTokenService fcmTokenService;
    private final NotificationService self;

    public NotificationService(NotificationRepository notificationRepository, FcmTokenService fcmTokenService, @Lazy NotificationService self) {
        this.notificationRepository = notificationRepository;
        this.fcmTokenService = fcmTokenService;
        this.self = self;
    }

    // 일기 알림 전송 메서드
    public void sendJournalNotification(User user) {
        String title = "칭찬일기 알림";
        String message = "오늘 하루는 어떠셨나요? 하루를 되돌아보며 칭찬일기를 적어보세요!";
        String url = "https://www.mannamdeliveries.link/Calendar";

        self.saveNotification(NotificationType.Journal, user, null, message);

        try {
            fcmTokenService.sendPushToUserAsync(user, title, message, url, null);
        } catch (Exception e) {
            log.error("Failed to send journal notification", e);
        }
    }

    @Transactional
    public void saveNotification(NotificationType type, User user, Long referenceId, String message) {
        Notification newNotification = Notification.builder()
                .type(type)
                .message(message)
                .referenceId(referenceId)
                .user(user)
                .build();
        notificationRepository.save(newNotification);
    }


    // 알림 추가
    @Transactional
    public void createNotification(NotificationType type, User user, Long referenceId) {
        String title = switch (type) {
            case Review -> "리뷰 알림";
            case Report -> "신고 안내";
            case Journal -> "칭찬일기 알림";
            case Meeting -> "만남 일정 알림";
            case Request -> "매칭 요청";
            case Match -> "매칭 성사";
            case InvitationRequest -> "만남 초대장";
            case InvitationAccepted -> "초대장 수락";
            case InvitationRejected -> "초대장 거절";
            case MeetingAccepted -> "만남 확정";
            case MeetingRejected -> "일정 조정 요청";
        };

        String message = switch (type) {
            case Review -> "상대방이 리뷰를 작성해주셨어요! 확인해보세요!";
            case Report -> "신고에 대한 답변이 도착했습니다.";
            case Journal -> "오늘 하루는 어떠셨나요? 하루를 되돌아보며 칭찬일기를 적어보세요!";
            case Meeting -> "내일 약속 있는거 기억하시죠? 준비 잘 하셔서 좋은 시간 보내시길 바래요!";
            case Request -> "어떤 분이 매칭 요청을 보냈어요!";
            case Match -> "매칭이 성사되었어요! 매칭 목록으로 가셔서 확인해보세요!";
            case InvitationRequest -> "만남 초대장을 전송했어요!";
            case InvitationAccepted -> "상대방이 만남 초대장을 수락했어요! 만남 일정을 정해보세요!";
            case InvitationRejected -> "상대방이 만남 초대장을 거절했어요...";
            case MeetingAccepted -> "만남 일정이 확정되었어요!";
            case MeetingRejected -> "상대방이 그 날짜에는 곤란한가봐요. 다시 일정을 정해봐요!";
        };

        String url = switch (type) {
            case Review -> "https://www.mannamdeliveries.link/profile";
            case Report -> "https://www.mannamdeliveries.link/report/";
            case Journal, Meeting -> "https://www.mannamdeliveries.link/Calendar";
            case Request, MeetingAccepted, MeetingRejected -> "https://www.mannamdeliveries.link/notifications";
            case Match -> "https://www.mannamdeliveries.link/ChatList";
            case InvitationRequest, InvitationAccepted, InvitationRejected -> "https://www.mannamdeliveries.link/";
        };

        Notification newNotification = Notification.builder()
                .type(type)
                .message(message)
                .referenceId(referenceId)
                .user(user)
                .build();

         notificationRepository.save(newNotification);

         fcmTokenService.sendPushToUserAsync(user, title, message, "https://www.mannamdeliveries.link", null);
    }

    // 알림 조회 (조회한 알림은 자동으로 읽음 처리)
    @Transactional
    public List<NotificationResponseDto> getNotifications(User user) {
        List<NotificationResponseDto> dtos = new ArrayList<>();

        List<Notification> notifications = notificationRepository.findNotificationByUser(user);

        for (Notification notification : notifications) {
            dtos.add(NotificationResponseDto.fromEntity(notification));
            notification.updateIsRead(true);
        }

        return dtos;
    }

    // 안 읽은 알람 존재 여부 조회
    @Transactional(readOnly = true)
    public boolean hasUnreadNotifications(User user) {
        return notificationRepository.existsByUserAndIsReadFalse(user);
    }

    // 알림 삭제 (단일)
    @Transactional
    public void deleteNotification(User user, long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow();

        if (!notification.getUser().equals(user)) {
            throw new IllegalStateException("해당 유저의 알림이 아닙니다.");
        }

        notificationRepository.delete(notification);
    }

    // 알림 삭제 (일괄)
    @Transactional
    public void deleteAllNotifications(User user) {
        notificationRepository.removeNotificationByUser(user);
    }
}
