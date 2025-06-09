package com.kslj.mannam.domain.chat.service;

import com.kslj.mannam.domain.chat.dto.ChatMessageDto;
import com.kslj.mannam.domain.chat.dto.ChatResponseDto;
import com.kslj.mannam.domain.chat.entity.Chat;
import com.kslj.mannam.domain.chat.repository.ChatRepository;
import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.enums.MatchStatus;
import com.kslj.mannam.domain.room.entity.Room;
import com.kslj.mannam.domain.room.repository.RoomRepository;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.firebase.FcmTokenService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final RoomRepository roomRepository;
    private final FcmTokenService fcmTokenService;

    // 메시지 저장
    @Transactional
    public long saveChatMessage(ChatMessageDto dto, long roomId, User sender) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        Match match = room.getMatch();

        User receiver;
        if (match.getUser1().equals(sender)) {
            receiver = match.getUser2();
        } else {
            receiver = match.getUser1();
        }

        Chat newChat = Chat.builder()
                .type(dto.getType())
                .message(dto.getMessage())
                .imageUrl(dto.getImageUrl())
                .room(room)
                .user(sender)
                .build();

        Chat savedChat = chatRepository.save(newChat);

        // 푸시 알림 전송
        String title = sender.getNickname();
        String body;
        if (dto.getMessage() == null) {
            body = "사진을 전송했습니다.";
        } else {
            body = dto.getMessage();
        }
        fcmTokenService.sendPushToUser(receiver, title, body, "https://www.mannamdeliveries.link/chat/" + roomId, String.valueOf(roomId));

        return savedChat.getId();
    }

    // 메시지 요청이 들어온 사용자가 채팅방에 참여중인 유저인지 검사
    @Transactional(readOnly = true)
    public boolean inspectUser(long roomId, User user) {
        Optional<Room> optionalRoom = roomRepository.findById(roomId);

        if (optionalRoom.isEmpty()) {
            log.error("Room ID {} not found in DB!", roomId);
            throw new EntityNotFoundException("Room not found: " + roomId);
        }

        Room room = optionalRoom.get();
        Match match = room.getMatch();

        if (match == null) {
            log.error("Match in Room ID {} is null!", roomId);
            throw new IllegalStateException("Match is null for room " + roomId);
        }

        return match.getUser1().equals(user) || match.getUser2().equals(user);
    }

    // 메시지 불러오기
    @Transactional(readOnly = true)
    public List<ChatResponseDto> getChatMessages(long roomId) {
        List<ChatResponseDto> dtos = new ArrayList<>();

        // 채팅방 조회
        Room room = roomRepository.findById(roomId).orElseThrow();

        // 해당 채팅방 채팅 이력 조회
        List<Chat> chatMessages = chatRepository.findChatByRoom(room);

        for (Chat chat : chatMessages) {
            dtos.add(ChatResponseDto.builder()
                    .type(chat.getType())
                    .message(chat.getMessage())
                    .imageUrl(chat.getImageUrl())
                    .sender(chat.getUser().getId())
                    .sentAt(chat.getSentAt())
                    .build());
        }

        return dtos;
    }

    // 채팅방 나가기
    @Transactional
    public void leaveRoom(long roomId, User user, String reasonCodes, String customReason) {
        // 매치 정보 가져오기
        Room room = roomRepository.findById(roomId).orElseThrow();
        Match match = room.getMatch();

        // 채팅 중 중단으로 상태 변경
        match.cancelMatch(user, MatchStatus.Chat_Cancelled, reasonCodes, customReason);
    }

    // 메시지 삭제

}
