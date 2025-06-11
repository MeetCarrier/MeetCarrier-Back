package com.kslj.mannam.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final UserService userService;
    private final FcmTokenRepository fcmTokenRepository;

    @Transactional
    public void saveOrUpdateToken(Long userId, String token) {
        log.info("userId = " + userId + ", token = " + token);
        User user = userService.getUserById(userId);
        Optional<FcmToken> existingTokenOpt = fcmTokenRepository.findByToken(token);

        if (existingTokenOpt.isPresent()) {
            // 토큰이 이미 존재하는 경우
            FcmToken existingToken = existingTokenOpt.get();

            // 하지만 해당 토큰이 다른 유저의 소유라면 현재 소유한 유저로 소유권 이전
            if (!existingToken.getUser().getId().equals(userId)) {
                existingToken.updateUser(user);
            }
            existingToken.updateUpdatedAt();

        } else {
            FcmToken newToken = FcmToken.builder()
                    .user(user)
                    .token(token)
                    .updatedAt(LocalDateTime.now())
                    .build();

            fcmTokenRepository.save(newToken);
        }
    }

    @Transactional
    public void deleteToken(String token) {
        fcmTokenRepository.deleteByToken(token);
    }

    @Async
    @Transactional
    public void sendPushToUserAsync(User user, String title, String body, String url, String chatRoomId) {
        try {
            List<FcmToken> tokens = fcmTokenRepository.findAllByUser(user);

            if (tokens.isEmpty()) {
                // 토큰이 없으면 로그만 남기고 종료
                log.info("No FCM token found for user: {}. Skipping async push.", user.getId());
                return; // ❗️ 예외를 던지지 않고 메소드 종료
            }

            for (FcmToken tokenEntity : tokens) {
                String token = tokenEntity.getToken();
                try {
                    Message.Builder messageBuilder = Message.builder()
                            .setToken(token)
                            .putData("title", title)
                            .putData("body", body)
                            .putData("url", url);

                    if (chatRoomId != null && !chatRoomId.isEmpty()) {
                        messageBuilder.putData("chatRoomId", chatRoomId);
                    }

                    Message message = messageBuilder.build();
                    FirebaseMessaging.getInstance().send(message);
                    log.info("Async push sent successfully To: {} for user: {}", token, user.getId());

                } catch (Exception e) {
                    // 개별 토큰 전송 실패가 전체 로직에 영향을 주지 않도록 내부에서 처리
                    log.warn("Failed to send message to a single token: {}. Error: {}", token, e.getMessage());
                    fcmTokenRepository.delete(tokenEntity);
                }
            }
        } catch (Exception e) {
            // 전체 로직 수행 중 예측하지 못한 에러 발생 시 로그
            log.error("An unexpected error occurred while sending push notifications asynchronously for user: {}", user.getId(), e);
        }
    }
}
