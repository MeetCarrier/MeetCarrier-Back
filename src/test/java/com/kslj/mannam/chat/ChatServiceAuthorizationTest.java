package com.kslj.mannam.chat;

import com.kslj.mannam.domain.chat.dto.ChatMessageDto;
import com.kslj.mannam.domain.chat.repository.ChatRepository;
import com.kslj.mannam.domain.chat.service.ChatPresenceService;
import com.kslj.mannam.domain.chat.service.ChatService;
import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.room.entity.Room;
import com.kslj.mannam.domain.room.repository.RoomRepository;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.firebase.FcmTokenService;
import com.kslj.mannam.redis.RedisUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ChatServiceAuthorizationTest {

    private ChatRepository chatRepository;
    private RoomRepository roomRepository;
    private RedisUtils redisUtils;
    private SimpMessagingTemplate messagingTemplate;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatRepository = mock(ChatRepository.class);
        roomRepository = mock(RoomRepository.class);
        redisUtils = mock(RedisUtils.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        chatService = new ChatService(
                chatRepository,
                roomRepository,
                mock(FcmTokenService.class),
                redisUtils,
                mock(ChatPresenceService.class),
                messagingTemplate
        );
    }

    @Test
    void 방참여자가아니면메시지를저장할수없다() {
        User user1 = user(1L);
        User user2 = user(2L);
        User attacker = user(3L);
        Room room = Room.builder()
                .id(10L)
                .match(Match.builder().user1(user1).user2(user2).build())
                .build();
        when(roomRepository.findById(10L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> chatService.saveChatMessage(
                ChatMessageDto.builder().build(), 10L, attacker, false))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(chatRepository);
    }

    @Test
    void 방참여자가아니면읽음처리를할수없다() {
        when(roomRepository.existsParticipant(10L, 3L)).thenReturn(false);

        assertThatThrownBy(() -> chatService.markMessagesAsRead(3L, 10L))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(chatRepository, redisUtils, messagingTemplate);
    }

    private User user(long id) {
        return User.builder().id(id).build();
    }
}
