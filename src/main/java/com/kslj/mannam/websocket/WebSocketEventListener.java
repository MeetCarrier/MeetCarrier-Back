package com.kslj.mannam.websocket;

import com.kslj.mannam.domain.match.service.MatchQueueManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final MatchQueueManager queueManager;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() != null) {
            String userIdStr = accessor.getUser().getName();
            try {
                long userId = Long.parseLong(userIdStr);
                if (queueManager.isUserInQueue(userId)) {
                    queueManager.cancelMatching(userId);
                    log.info("❌ 매칭 중인 유저 {} 연결 종료 -> 매칭 취소", userId);
                }
            } catch (NumberFormatException e) {
                log.warn("잘못된 사용자 ID 형식: {}", userIdStr);
            }
        }
    }
}
