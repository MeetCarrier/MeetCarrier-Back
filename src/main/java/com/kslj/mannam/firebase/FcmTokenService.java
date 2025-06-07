package com.kslj.mannam.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
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
        User user = userService.getUserById(userId);

        Optional<FcmToken> existing = fcmTokenRepository.findByToken(token);

        if (existing.isPresent()) {
            existing.get().updateUpdatedAt();
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
    public void sendPushToUser(User user, String title, String body, String url) {
        List<FcmToken> tokens = fcmTokenRepository.findAllByUser(user);

        if (tokens.isEmpty()) {
            throw new IllegalStateException("No FCM token found for user: " + user);
        }

        for (FcmToken tokenEntity : tokens) {
            String token = tokenEntity.getToken();
            try {
                Message message = Message.builder()
                        .setToken(token)
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .putData("url", url)
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
                log.info("Sent message: " + response + " To: " + user.getId());
            } catch (Exception e) {
                log.warn("Failed to send message: " + e.getMessage());
            }
        }
    }
}
