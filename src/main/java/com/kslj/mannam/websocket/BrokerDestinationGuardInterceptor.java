package com.kslj.mannam.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class BrokerDestinationGuardInterceptor implements ChannelInterceptor {

    private static final String TOPIC_PREFIX = "/topic";
    private static final String QUEUE_PREFIX = "/queue";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() != StompCommand.SEND) {
            return message;
        }

        String destination = accessor.getDestination();
        if (isBrokerDestination(destination)) {
            throw new AccessDeniedException("브로커 목적지로 직접 메시지를 전송할 수 없습니다.");
        }

        return message;
    }

    private boolean isBrokerDestination(String destination) {
        return matchesPrefix(destination, TOPIC_PREFIX) || matchesPrefix(destination, QUEUE_PREFIX);
    }

    private boolean matchesPrefix(String destination, String prefix) {
        return destination != null
                && (destination.equals(prefix) || destination.startsWith(prefix + "/"));
    }
}
