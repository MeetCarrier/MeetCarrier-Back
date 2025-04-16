package com.kslj.mannam.domain.chat.service;

import com.kslj.mannam.domain.chat.dto.ChatMessageDto;
import com.kslj.mannam.domain.chat.dto.ChatResponseDto;
import com.kslj.mannam.domain.chat.entity.Chat;
import com.kslj.mannam.domain.chat.repository.ChatRepository;
import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.room.entity.Room;
import com.kslj.mannam.domain.room.repository.RoomRepository;
import com.kslj.mannam.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final RoomRepository roomRepository;

    // 메시지 저장
    public long saveChatMessage(ChatMessageDto dto, long roomId, User user) {
        Room room = roomRepository.findById(roomId).orElseThrow();

        Chat newChat = Chat.builder()
                .type(dto.getType())
                .message(dto.getMessage())
                .imageUrl(dto.getImageUrl())
                .room(room)
                .user(user)
                .build();

        Chat savedChat = chatRepository.save(newChat);

        return savedChat.getId();
    }

    // 메시지 요청이 들어온 사용자가 채팅방에 참여중인 유저인지 검사
    public boolean inspectUser(long roomId, User sender) {
        // 채팅방 조회
        Room room = roomRepository.findRoomWithMatchAndUsers(roomId).orElseThrow();

        // 참여자 확인
        Match match = room.getMatch();

        return match.hasUser(sender);
    }

    // 메시지 불러오기
    public List<ChatResponseDto> getChatMessages(long roomId) {
        List<ChatResponseDto> dtos = new ArrayList<>();

        // 채팅방 조회
        Room room = roomRepository.findById(roomId).orElseThrow();

        // 채팅방 조회
        List<Chat> chatMessages = chatRepository.findChatByRoom(room);

        for (Chat chat : chatMessages) {
            dtos.add(ChatResponseDto.builder()
                    .messageType(chat.getType())
                    .message(chat.getMessage())
                    .imageUrl(chat.getImageUrl())
                    .sender(chat.getUser().getId())
                    .build());
        }

        return dtos;
    }

    // 메시지 삭제

}
