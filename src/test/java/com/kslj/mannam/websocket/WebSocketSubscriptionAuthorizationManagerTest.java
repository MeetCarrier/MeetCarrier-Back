package com.kslj.mannam.websocket;

import com.kslj.mannam.domain.room.repository.RoomRepository;
import com.kslj.mannam.domain.survey.repository.SurveySessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.messaging.simp.SimpMessageHeaderAccessor.DESTINATION_HEADER;
import static org.springframework.messaging.simp.SimpMessageHeaderAccessor.MESSAGE_TYPE_HEADER;

class WebSocketSubscriptionAuthorizationManagerTest {

    private RoomRepository roomRepository;
    private SurveySessionRepository surveySessionRepository;
    private WebSocketSubscriptionAuthorizationManager authorizationManager;

    @BeforeEach
    void setUp() {
        roomRepository = mock(RoomRepository.class);
        surveySessionRepository = mock(SurveySessionRepository.class);
        authorizationManager = new WebSocketSubscriptionAuthorizationManager(
                roomRepository, surveySessionRepository);
    }

    @Test
    void 방참여자는방과읽음채널을구독할수있다() {
        when(roomRepository.existsParticipant(10L, 1L)).thenReturn(true);

        assertThat(check("1", "/topic/room/10").isGranted()).isTrue();
        assertThat(check("1", "/topic/room/10/read").isGranted()).isTrue();
        verify(roomRepository, times(2)).existsParticipant(10L, 1L);
    }

    @Test
    void 방참여자가아니면구독할수없다() {
        when(roomRepository.existsParticipant(10L, 2L)).thenReturn(false);

        assertThat(check("2", "/topic/room/10").isGranted()).isFalse();
    }

    @Test
    void 설문참여자만설문채널을구독할수있다() {
        when(surveySessionRepository.existsParticipant(20L, 1L)).thenReturn(true);

        assertThat(check("1", "/topic/survey/20/complete").isGranted()).isTrue();
        assertThat(check("2", "/topic/survey/20/leave").isGranted()).isFalse();
    }

    @Test
    void 인증사용자는자신의UserDestination을구독할수있다() {
        assertThat(check("1", "/user/queue/chats").isGranted()).isTrue();
        assertThat(check("1", "/user/queue/assistant").isGranted()).isTrue();
        assertThat(check("1", "/user/queue/match-result").isGranted()).isTrue();
    }

    @Test
    void 기존UserId기반개인Topic은거부한다() {
        assertThat(check("1", "/topic/user/1/chats").isGranted()).isFalse();
        assertThat(check("1", "/topic/assistant/1").isGranted()).isFalse();
    }

    @Test
    void 인증되지않은사용자와알수없는목적지는거부한다() {
        Authentication unauthenticated = new TestingAuthenticationToken("1", null);
        unauthenticated.setAuthenticated(false);

        assertThat(check(() -> unauthenticated, "/topic/room/10").isGranted()).isFalse();
        assertThat(check("1", "/topic/unknown").isGranted()).isFalse();
    }

    @Test
    void 인증된사용자만연결할수있다() {
        assertThat(check("1", SimpMessageType.CONNECT, null).isGranted()).isTrue();

        Authentication unauthenticated = new TestingAuthenticationToken("1", null);
        unauthenticated.setAuthenticated(false);
        assertThat(check(() -> unauthenticated, SimpMessageType.CONNECT, null).isGranted()).isFalse();
    }

    @Test
    void 인증된사용자는허용된Application목적지로만전송할수있다() {
        assertThat(check("1", SimpMessageType.MESSAGE, "/app/api/chat/send").isGranted()).isTrue();
        assertThat(check("1", SimpMessageType.MESSAGE, "/app/not-mapped").isGranted()).isFalse();
        assertThat(check("1", SimpMessageType.MESSAGE, "/topic/room/10").isGranted()).isFalse();
    }

    private AuthorizationDecision check(String userId, String destination) {
        return check(() -> new TestingAuthenticationToken(userId, null, "ROLE_USER"), destination);
    }

    private AuthorizationDecision check(Supplier<Authentication> authentication, String destination) {
        return check(authentication, SimpMessageType.SUBSCRIBE, destination);
    }

    private AuthorizationDecision check(String userId, SimpMessageType messageType, String destination) {
        return check(() -> new TestingAuthenticationToken(userId, null, "ROLE_USER"), messageType, destination);
    }

    private AuthorizationDecision check(
            Supplier<Authentication> authentication,
            SimpMessageType messageType,
            String destination
    ) {
        Message<byte[]> message = MessageBuilder.withPayload(new byte[0])
                .setHeader(MESSAGE_TYPE_HEADER, messageType)
                .setHeader(DESTINATION_HEADER, destination)
                .build();
        return authorizationManager.check(authentication, message);
    }
}
