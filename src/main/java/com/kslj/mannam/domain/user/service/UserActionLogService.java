package com.kslj.mannam.domain.user.service;

import com.kslj.mannam.domain.review.entity.Review;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.entity.UserActionLog;
import com.kslj.mannam.domain.user.enums.ActionType;
import com.kslj.mannam.domain.user.repository.UserActionLogRepository;
import com.kslj.mannam.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserActionLogService {

    private final UserActionLogRepository userActionLogRepository;
    private final UserRepository userRepository;

    // 사용자 행동 기록 (하루에 1개만)
    @Transactional
    public void logUserAction(User user, ActionType actionType) {
        LocalDate today = LocalDate.now();
        boolean exists = userActionLogRepository.existsByUserAndActionTypeAndActionDate(user, actionType, today);
        if (!exists) {
            double actionScore = 0;
            switch (actionType) {
                case WRITE_JOURNAL, TEST_DONE -> actionScore = 1.0;
                case COMPLETE_MATCH, COMPLETE_SURVEY -> actionScore = 2.0;
                case COMPLETE_MEETING -> actionScore = 5.0;
            }

            userActionLogRepository.save(UserActionLog.builder()
                    .actionType(actionType)
                    .actionDate(today)
                    .actionScore(actionScore)
                    .user(user)
                    .build()
            );
        }
    }

    // 사용자가 받은 리뷰 점수 기록
    @Transactional
    public void logUserReview(User user, Review review) {
        LocalDate today = LocalDate.now();
        double actionScore = review.getRating();
        userActionLogRepository.save(UserActionLog.builder()
                .actionType(ActionType.GET_REVIEW)
                .actionDate(today)
                .actionScore(actionScore)
                .user(user)
                .build()
        );
    }

    // 매 자정마다 만남발자국 갱신
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateFootprint() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            double newFootprint = user.getFootprint() * 0.9;
            user.updateFootprint(newFootprint);
        }

        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<UserActionLog> logs = userActionLogRepository.findByActionDate(yesterday);

        Map<Long, Double> footprintMap = new HashMap<>();

        for (UserActionLog log : logs) {
            double increment = log.getActionScore();
            footprintMap.merge(log.getUser().getId(), increment, Double::sum);
        }

        List<User> targetUsers = userRepository.findAllById(footprintMap.keySet());
        for (User user : targetUsers) {
            double increment = footprintMap.get(user.getId());
            user.updateFootprint(user.getFootprint() + increment);
        }
    }
}
