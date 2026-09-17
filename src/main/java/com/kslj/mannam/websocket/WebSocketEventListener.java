package com.kslj.mannam.websocket;

import com.kslj.mannam.domain.chat.service.ChatPresenceService;
import com.kslj.mannam.domain.match.service.MatchOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private static final String ROOM_TOPIC_PREFIX = "/topic/room/";

    private final MatchOrchestrationService matchOrchestrationService;
    private final ChatPresenceService chatPresenceService;

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();

        if (destination == null || !destination.matches("^/topic/room/\\d+$")) {
            return;
        }
        if (accessor.getUser() == null || accessor.getUser().getName() == null) {
            return;
        }

        try {
            Long roomId = Long.parseLong(destination.substring(ROOM_TOPIC_PREFIX.length()));
            Long userId = Long.parseLong(accessor.getUser().getName());

            chatPresenceService.addSubscription(
                    roomId,
                    userId,
                    accessor.getSessionId(),
                    accessor.getSubscriptionId()
            );
            log.info(
                    "USER SUBSCRIBED: userId={} joined roomId={}, sessionId={}, subscriptionId={}",
                    userId,
                    roomId,
                    accessor.getSessionId(),
                    accessor.getSubscriptionId()
            );
        } catch (NumberFormatException e) {
            log.warn("Invalid user id or room id. userId={}, destination={}", accessor.getUser().getName(), destination);
        }
    }

    @EventListener
    public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        chatPresenceService.removeSubscription(accessor.getSessionId(), accessor.getSubscriptionId());
        log.info(
                "USER UNSUBSCRIBED: sessionId={}, subscriptionId={}",
                accessor.getSessionId(),
                accessor.getSubscriptionId()
        );
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        chatPresenceService.removeSession(sessionId);
        log.info("WEBSOCKET SESSION DISCONNECTED: sessionId={}", sessionId);

        if (accessor.getUser() == null || accessor.getUser().getName() == null) {
            return;
        }

        String userIdValue = accessor.getUser().getName();
        try {
            long userId = Long.parseLong(userIdValue);
            if (matchOrchestrationService.isUserInQueue(userId)) {
                matchOrchestrationService.cancelMatching(userId);
                log.info("매칭 중인 유저 {} 연결 종료 -> 매칭 취소", userId);
            }
        } catch (NumberFormatException e) {
            log.warn("잘못된 사용자 ID 형식: {}", userIdValue);
        }
    }
}
