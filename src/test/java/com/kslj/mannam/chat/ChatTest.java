package com.kslj.mannam.chat;

import com.kslj.mannam.TestUtils;
import com.kslj.mannam.domain.chat.dto.ChatMessageDto;
import com.kslj.mannam.domain.chat.dto.ChatResponseDto;
import com.kslj.mannam.domain.chat.enums.MessageType;
import com.kslj.mannam.domain.chat.service.ChatService;
import com.kslj.mannam.domain.match.dto.MatchCreateDto;
import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.repository.MatchRepository;
import com.kslj.mannam.domain.match.service.MatchService;
import com.kslj.mannam.domain.room.service.RoomService;
import com.kslj.mannam.domain.user.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
public class ChatTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TestUtils testUtils;

    // ChatMessageDto 생성 메서드
    private ChatMessageDto createChatMessageDto(long roomId, MessageType type, String message, String imageUrl) {
        ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                .roomId(roomId)
                .type(type)
                .message(message)
                .imageUrl(imageUrl)
                .build();

        return chatMessageDto;
    }

    // 매칭 생성 메서드
    private long createMatch(User user1, User user2) {
        MatchCreateDto matchCreateDto = MatchCreateDto.builder()
                .score(90)
                .user1Id(user1.getId())
                .user2Id(user2.getId())
                .build();

        return matchService.createMatch(matchCreateDto);
    }

    // 채팅방 생성 메서드
    private long createRoom(Match match) {
        return roomService.createRoom(match);
    }

    // 채팅 데이터 저장 테스트
    @Test
    public void testCreateChatMessage() {
        //given
        User foundUser1 = testUtils.createAndGetTestUser();
        User foundUser2 = testUtils.createAndGetTestUser();
        long matchId = createMatch(foundUser1, foundUser2);
        long roomId = createRoom(matchRepository.getMatchById((matchId)));
        ChatMessageDto messageDto = createChatMessageDto(roomId, MessageType.TEXT, "안녕", "");

        //when
        long savedChatId = chatService.saveChatMessage(messageDto, roomId, foundUser1);
        List<ChatResponseDto> chatMessages = chatService.getChatMessages(messageDto.getRoomId());

        //then
        System.out.println("chatMessages.get(0) = " + chatMessages.get(0));
        Assertions.assertEquals(messageDto.getMessage(), chatMessages.get(0).getMessage());
    }

    // roomId 기준으로 채팅 데이터 조회 테스트
    @Test
    public void getChatMessages() {
        // given
        User foundUser1 = testUtils.createAndGetTestUser();
        User foundUser2 = testUtils.createAndGetTestUser();
        long matchId = createMatch(foundUser1, foundUser2);
        long roomId = createRoom(matchRepository.getMatchById((matchId)));
        ChatMessageDto messageDto1 = createChatMessageDto(roomId, MessageType.TEXT, "안녕", "");
        ChatMessageDto messageDto2 = createChatMessageDto(roomId, MessageType.TEXT, "좋은밤", "");
        ChatMessageDto messageDto3 = createChatMessageDto(roomId, MessageType.TEXT, "잘하시네요", "");

        long savedChatId1 = chatService.saveChatMessage(messageDto1, roomId, foundUser2);
        long savedChatId2 = chatService.saveChatMessage(messageDto2, roomId, foundUser1);
        long savedChatId3 = chatService.saveChatMessage(messageDto3, roomId, foundUser2);

        User foundUser3 = testUtils.createAndGetTestUser();
        User foundUser4 = testUtils.createAndGetTestUser();
        long matchId2 = createMatch(foundUser3, foundUser4);
        long roomId2 = createRoom(matchRepository.getMatchById((matchId2)));
        ChatMessageDto messageDto4 = createChatMessageDto(roomId2, MessageType.TEXT, "hello", "");
        ChatMessageDto messageDto5 = createChatMessageDto(roomId2, MessageType.TEXT, "nice to meet you", "");
        ChatMessageDto messageDto6 = createChatMessageDto(roomId2, MessageType.TEXT, "it's good time to talk", "");
        ChatMessageDto messageDto7 = createChatMessageDto(roomId2, MessageType.TEXT, "yeah.", "");
        ChatMessageDto messageDto8 = createChatMessageDto(roomId2, MessageType.TEXT, "how was your today", "");

        long savedChatId4 = chatService.saveChatMessage(messageDto4, roomId2, foundUser3);
        long savedChatId5 = chatService.saveChatMessage(messageDto5, roomId2, foundUser3);
        long savedChatId6 = chatService.saveChatMessage(messageDto6, roomId2, foundUser4);
        long savedChatId7 = chatService.saveChatMessage(messageDto7, roomId2, foundUser4);
        long savedChatId8 = chatService.saveChatMessage(messageDto8, roomId2, foundUser3);

        // when
        List<ChatResponseDto> messages1 = chatService.getChatMessages(roomId);
        List<ChatResponseDto> messages2 = chatService.getChatMessages(roomId2);

        // then
        Assertions.assertEquals(3, messages1.size());
        Assertions.assertEquals(5, messages2.size());

    }


    // 채팅 송신자가 채팅방에 있는 사람인지 검사
    @Test
    public void inspectionTest() {
        // given
        //given
        User foundUser1 = testUtils.createAndGetTestUser();
        User foundUser2 = testUtils.createAndGetTestUser();
        long matchId = createMatch(foundUser1, foundUser2);
        long roomId = createRoom(matchRepository.getMatchById((matchId)));
        ChatMessageDto messageDto = createChatMessageDto(roomId, MessageType.TEXT, "안녕", "");

        // when
        User user1 = testUtils.createAndGetTestUser();
        boolean b = chatService.inspectUser(roomId, user1);

        // then
        Assertions.assertFalse(b);
    }
}