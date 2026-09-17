package com.kslj.mannam.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrokerDestinationGuardInterceptorTest {

    private final BrokerDestinationGuardInterceptor interceptor = new BrokerDestinationGuardInterceptor();

    @Test
    void topicDestination으로직접전송할수없다() {
        Message<byte[]> message = stompMessage(StompCommand.SEND, "/topic/room/1");

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void queueDestination으로직접전송할수없다() {
        Message<byte[]> message = stompMessage(StompCommand.SEND, "/queue/private");

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void applicationDestination전송은허용한다() {
        Message<byte[]> message = stompMessage(StompCommand.SEND, "/app/api/chat/send");

        assertThat(interceptor.preSend(message, null)).isSameAs(message);
    }

    @Test
    void topicDestination구독은이번규칙에서차단하지않는다() {
        Message<byte[]> message = stompMessage(StompCommand.SUBSCRIBE, "/topic/room/1");

        assertThat(interceptor.preSend(message, null)).isSameAs(message);
    }

    private Message<byte[]> stompMessage(StompCommand command, String destination) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setDestination(destination);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
