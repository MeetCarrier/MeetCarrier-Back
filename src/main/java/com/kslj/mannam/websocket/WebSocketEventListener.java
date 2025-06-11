package com.kslj.mannam.websocket;

import com.kslj.mannam.domain.chat.service.ChatPresenceService;
import com.kslj.mannam.domain.chat.service.ChatService;
import com.kslj.mannam.domain.match.service.MatchQueueManager;
import com.kslj.mannam.domain.match.service.MatchService;
import com.kslj.mannam.redis.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final MatchQueueManager queueManager;
    private final ChatPresenceService chatPresenceService;
    private final ChatService chatService;
    private final MatchService matchService;
    private final RedisUtils redisUtils; // ✨ RedisUtils 주입

    // 구독 시, Redis에 구독 정보 저장
    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        String sessionId = accessor.getSessionId();
        String subscriptionId = accessor.getSubscriptionId();
        // user가 null일 경우를 대비해 Objects.requireNonNull 사용
        String currentUserIdStr = Objects.requireNonNull(accessor.getUser()).getName();

        // 채팅방 구독인 경우에만 처리
        if (destination != null && destination.matches("^/topic/room/\\d+$")) {
            try {
                Long roomId = Long.parseLong(destination.substring("/topic/room/".length()));
                Long userId = Long.parseLong(currentUserIdStr);

                // 1. 기존의 접속 처리 로직 수행
                chatPresenceService.userJoined(roomId, userId);
                log.info("USER SUBSCRIBED: userId={} joined roomId={}", userId, roomId);

                // 2. ✨ RedisUtils를 사용하여 구독 정보 저장 (Key: 세션ID+구독ID, Value: 방ID)
                String redisKey = createRedisKey(sessionId, subscriptionId);
                // 12시간 유효시간 설정
                long expiredTimeInSeconds = 12 * 60 * 60;
                redisUtils.setData(redisKey, String.valueOf(roomId), expiredTimeInSeconds);
                log.info("REDIS SET: key={}, value={}", redisKey, roomId);

                // 3. 읽음 처리 로직
                chatService.markMessagesAsRead(userId, roomId);

            } catch (NumberFormatException e) {
                log.warn("Invalid room id in destination: {}", destination);
            }
        }
    }

    // ✨ [신규] 구독 해제 시, Redis 정보를 바탕으로 퇴장 처리
    @EventListener
    public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String subscriptionId = accessor.getSubscriptionId();
        if (accessor.getUser() == null || accessor.getUser().getName() == null) {
            return;
        }
        String currentUserIdStr = accessor.getUser().getName();

        // Redis에서 해당 구독 정보를 조회
        String redisKey = createRedisKey(sessionId, subscriptionId);
        redisUtils.getData(redisKey).ifPresent(value -> {
            // Optional이 비어있지 않을 경우에만 이 블록이 실행됨
            String roomIdStr = String.valueOf(value); // Object를 String으로 변환

            try {
                Long roomId = Long.parseLong(roomIdStr);
                Long userId = Long.parseLong(currentUserIdStr);

                // 1. 실제 퇴장 로직 처리
                chatPresenceService.userLeft(roomId, userId);
                log.info("USER UNSUBSCRIBED: userId={} left roomId={}", userId, roomId);

                // 2. 처리 완료 후 Redis에서 정보 삭제
                redisUtils.deleteData(redisKey);
                log.info("REDIS DEL: key={}", redisKey);

            } catch (NumberFormatException e) {
                log.warn("Invalid userId or roomId format. userId={}, roomId={}", currentUserIdStr, roomIdStr);
            }
        });
    }

    // 연결 종료 시, 모든 정보 정리 (최후의 방어선)
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() != null && accessor.getUser().getName() != null) {
            String userIdStr = accessor.getUser().getName();
            log.info("userId={} disconnected", userIdStr);
            try {
                long userId = Long.parseLong(userIdStr);

                // 1. 매칭 큐에 있다면 매칭 취소 처리
                if (queueManager.isUserInQueue(userId)) {
                    queueManager.cancelMatching(userId);
                    log.info("❌ 매칭 중인 유저 {} 연결 종료 -> 매칭 취소", userId);
                }

                // 2. DB 기반의 퇴장 처리 (기존 로직 유지)
                List<Long> roomIds = matchService.getRoomByUserId(userId);
                if (roomIds != null) {
                    roomIds.stream().filter(Objects::nonNull).forEach(roomId -> {
                        chatPresenceService.userLeft(roomId, userId);
                        log.info("User {} left room {} due to DISCONNECT", userId, roomId);
                    });
                }

                // 참고: Redis의 남은 키들은 setData 시 설정한 TTL에 의해 자동으로 소멸되므로,
                // Disconnect 시 별도의 Redis 정리 로직은 필수가 아닙니다.

            } catch (NumberFormatException e) {
                log.warn("잘못된 사용자 ID 형식: {}", userIdStr);
            }
        }
    }

    /**
     * Redis 키를 생성하는 헬퍼 메소드
     */
    private String createRedisKey(String sessionId, String subscriptionId) {
        return "ws-session:" + sessionId + ":sub:" + subscriptionId;
    }
}