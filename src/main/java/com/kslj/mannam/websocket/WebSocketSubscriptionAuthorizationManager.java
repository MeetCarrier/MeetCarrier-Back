package com.kslj.mannam.websocket;

import com.kslj.mannam.domain.room.repository.RoomRepository;
import com.kslj.mannam.domain.survey.repository.SurveySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class WebSocketSubscriptionAuthorizationManager implements AuthorizationManager<Message<?>> {

    private static final Pattern ROOM_DESTINATION =
            Pattern.compile("^/topic/room/(\\d+)(?:/read)?$");
    private static final Pattern SURVEY_DESTINATION =
            Pattern.compile("^/topic/survey/(\\d+)/(?:leave|complete)$");
    private static final Set<String> APPLICATION_DESTINATIONS = Set.of(
            "/app/api/chat/send",
            "/app/api/chat/read",
            "/app/api/chat/leave",
            "/app/api/chatbot/send",
            "/app/api/assistant/send",
            "/app/api/survey/leave",
            "/app/api/start-matching"
    );
    private final RoomRepository roomRepository;
    private final SurveySessionRepository surveySessionRepository;

    @Override
    @SuppressWarnings("deprecation") // Spring Security 6.4의 AuthorizationManager 구현 메서드
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, Message<?> message) {
        SimpMessageType messageType = message.getHeaders()
                .get(SimpMessageHeaderAccessor.MESSAGE_TYPE_HEADER, SimpMessageType.class);
        if (messageType == null) {
            return new AuthorizationDecision(false);
        }

        Authentication authentication = authenticationSupplier.get();
        Long currentUserId = getAuthenticatedUserId(authentication);
        String destination = message.getHeaders()
                .get(SimpMessageHeaderAccessor.DESTINATION_HEADER, String.class);

        return switch (messageType) {
            case CONNECT -> new AuthorizationDecision(currentUserId != null);
            case MESSAGE -> new AuthorizationDecision(
                    currentUserId != null && APPLICATION_DESTINATIONS.contains(destination)
            );
            case SUBSCRIBE -> new AuthorizationDecision(
                    currentUserId != null && isSubscriptionAllowed(destination, currentUserId)
            );
            case UNSUBSCRIBE, DISCONNECT, HEARTBEAT -> new AuthorizationDecision(true);
            default -> new AuthorizationDecision(false);
        };
    }

    private boolean isSubscriptionAllowed(String destination, long currentUserId) {
        if (destination == null) {
            return false;
        }
        if (destination.startsWith("/user/")) {
            return true;
        }

        Matcher roomMatcher = ROOM_DESTINATION.matcher(destination);
        if (roomMatcher.matches()) {
            long roomId = Long.parseLong(roomMatcher.group(1));
            return roomRepository.existsParticipant(roomId, currentUserId);
        }

        Matcher surveyMatcher = SURVEY_DESTINATION.matcher(destination);
        if (surveyMatcher.matches()) {
            long sessionId = Long.parseLong(surveyMatcher.group(1));
            return surveySessionRepository.existsParticipant(sessionId, currentUserId);
        }

        return false;
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
