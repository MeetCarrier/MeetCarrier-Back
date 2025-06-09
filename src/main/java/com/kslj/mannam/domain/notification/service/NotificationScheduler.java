package com.kslj.mannam.domain.notification.service;

import com.kslj.mannam.domain.journal.repository.JournalRepository;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final JournalRepository journalRepository;
    private final UserRepository userRepository;

    // 매일 20시마다 칭찬일기를 작성하지 않은 유저들에게 알림 전송
    @Scheduled(cron = "0 0 11 * * *")
    public void notifyDiary() {
        // 오늘 날짜 세팅
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        // 모든 유저 조회
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            boolean hasJournalToday = journalRepository.existsByUserAndCreatedAtBetween(user, startOfDay, endOfDay);

            if (!hasJournalToday) {
                notificationService.sendJournalNotification(user);
            }
        }
    }
}
