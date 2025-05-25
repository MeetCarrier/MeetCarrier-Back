package com.kslj.mannam.domain.notification.service;

import com.kslj.mannam.domain.notification.dto.NotificationResponseDto;
import com.kslj.mannam.domain.notification.entity.Notification;
import com.kslj.mannam.domain.notification.enums.NotificationType;
import com.kslj.mannam.domain.notification.repository.NotificationRepository;
import com.kslj.mannam.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    // 알림 추가
    @Transactional
    public void createNotification(NotificationType type, User user, Long referenceId) {
        String message = "";

        switch (type) {
            case Review -> message = "상대방이 리뷰를 작성해주셨어요! 확인해보세요!";
            case Report -> message = "신고에 대한 답변이 도착했습니다.";
            case Journal -> message = "오늘 하루는 어떠셨나요? 하루를 되돌아보며 칭찬일기를 적어보세요!";
            case Meeting -> message = "내일 약속 있는거 기억하시죠? 준비 잘 하셔서 좋은 시간 보내시길 바래요!";
        }

        Notification newNotification = Notification.builder()
                .type(type)
                .message(message)
                .referenceId(referenceId)
                .user(user)
                .build();

         notificationRepository.save(newNotification);
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
