package com.kslj.mannam.chat;

import com.kslj.mannam.domain.chat.service.ChatPresenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatPresenceServiceTest {

    private ChatPresenceService chatPresenceService;

    @BeforeEach
    void setUp() {
        chatPresenceService = new ChatPresenceService();
    }

    @Test
    void 동일한_세션의_구독이_하나라도_남아있으면_접속_상태를_유지한다() {
        chatPresenceService.addSubscription(10L, 1L, "session-1", "subscription-1");
        chatPresenceService.addSubscription(10L, 1L, "session-1", "subscription-2");

        chatPresenceService.removeSubscription("session-1", "subscription-1");

        assertThat(chatPresenceService.isUserActive(10L, 1L)).isTrue();

        chatPresenceService.removeSubscription("session-1", "subscription-2");

        assertThat(chatPresenceService.isUserActive(10L, 1L)).isFalse();
    }

    @Test
    void 다른_세션이_남아있으면_한_세션이_종료되어도_접속_상태를_유지한다() {
        chatPresenceService.addSubscription(10L, 1L, "session-1", "subscription-1");
        chatPresenceService.addSubscription(10L, 1L, "session-2", "subscription-1");

        chatPresenceService.removeSession("session-1");

        assertThat(chatPresenceService.isUserActive(10L, 1L)).isTrue();

        chatPresenceService.removeSession("session-2");

        assertThat(chatPresenceService.isUserActive(10L, 1L)).isFalse();
    }

    @Test
    void 세션이_종료되면_그_세션의_모든_채팅방_구독을_제거한다() {
        chatPresenceService.addSubscription(10L, 1L, "session-1", "subscription-1");
        chatPresenceService.addSubscription(20L, 1L, "session-1", "subscription-2");

        chatPresenceService.removeSession("session-1");

        assertThat(chatPresenceService.isUserActive(10L, 1L)).isFalse();
        assertThat(chatPresenceService.isUserActive(20L, 1L)).isFalse();
    }

    @Test
    void 같은_세션과_구독_ID가_재사용되면_기존_구독을_새_구독으로_교체한다() {
        chatPresenceService.addSubscription(10L, 1L, "session-1", "subscription-1");

        chatPresenceService.addSubscription(20L, 1L, "session-1", "subscription-1");

        assertThat(chatPresenceService.isUserActive(10L, 1L)).isFalse();
        assertThat(chatPresenceService.isUserActive(20L, 1L)).isTrue();
    }
}
