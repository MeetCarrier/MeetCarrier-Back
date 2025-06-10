package com.kslj.mannam.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional(readOnly = true)
    public void sendPushToUser(User user, String title, String body, String url, String chatRoomId) {
        List<FcmToken> tokens = fcmTokenRepository.findAllByUser(user);

        if (tokens.isEmpty()) {
            throw new IllegalStateException("No FCM token found for user: " + user);
        }

        for (FcmToken tokenEntity : tokens) {
            String token = tokenEntity.getToken();
            try {
                Message.Builder messageBuilder = Message.builder()
                        .setToken(token)
                        .putData("title", title)
                        .putData("body", body)
                        .putData("url", url);

                // 채팅 알림의 경우 채팅방 Id 데이터 추가
                if (chatRoomId != null && !chatRoomId.isEmpty()) {
                    messageBuilder.putData("chatRoomId", chatRoomId);
                }

                Message message = messageBuilder.build();
                String response = FirebaseMessaging.getInstance().send(message);
                log.info("Sent message: " + response + " To: " + user.getId());
            } catch (Exception e) {
                log.warn("Failed to send message: " + e.getMessage());
            }
        }
    }

    @Transactional
    public void deleteToken(String token) {
        fcmTokenRepository.deleteByToken(token);
    }
}
