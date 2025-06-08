package com.kslj.mannam.domain.chat.service;

import com.kslj.mannam.domain.chat.dto.ChatMessageDto;
import com.kslj.mannam.domain.chat.dto.ChatResponseDto;
import com.kslj.mannam.domain.chat.dto.LastChatDto;
import com.kslj.mannam.domain.chat.entity.Chat;
import com.kslj.mannam.domain.chat.repository.ChatRepository;
import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.enums.MatchStatus;
import com.kslj.mannam.domain.room.entity.Room;
import com.kslj.mannam.domain.room.repository.RoomRepository;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.firebase.FcmTokenService;
import com.kslj.mannam.redis.RedisUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final RedisUtils redisUtils;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatPresenceService chatPresenceService;
    private final UserService userService;

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

        // 상대방이 채팅방에 접속해있는지 확인
        boolean isReceiverOnline = chatPresenceService.isUserActive(roomId, receiver.getId());

        Chat newChat = Chat.builder()
                .type(dto.getType())
                .message(dto.getMessage())
                .imageUrl(dto.getImageUrl())
                .room(room)
                .user(sender)
                .isRead(isReceiverOnline)
                .build();

        Chat savedChat = chatRepository.save(newChat);

        if (!isReceiverOnline) {
            // Redis에 최근 채팅, 안 읽은 알람 개수 저장
            redisUtils.setData("chat:latest:" + roomId, dto.getMessage(), 3600 * 24);
            String unreadCountKey = "chat:unread:" + roomId + ":" + receiver.getId();
            Long unreadCount = redisUtils.incrData(unreadCountKey);

            // 푸시 알림 전송
            String title = sender.getNickname();
            String body;
            if (dto.getMessage() == null) {
                body = "사진을 전송했습니다.";
            } else {
                body = dto.getMessage();
            }
            fcmTokenService.sendPushToUser(receiver, title, body, "https://www.mannamdeliveries.link/chat/" + roomId);

            // 최근 채팅, 안 읽은 알람 개수 클라이언트에게 전송
            LastChatDto lastChatInfo = LastChatDto.builder()
                    .roomId(roomId)
                    .lastMessage(dto.getMessage())
                    .unreadCount(unreadCount.intValue())
                    .build();

            simpMessagingTemplate.convertAndSend("/topic/user/" + receiver.getId() + "/chats", lastChatInfo);
        }

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
    public void leaveRoom(long roomId, User user, String reason) {
        // 매치 정보 가져오기
        Room room = roomRepository.findById(roomId).orElseThrow();
        Match match = room.getMatch();

        // 채팅 중 중단으로 상태 변경
        match.cancelMatch(user, MatchStatus.Chat_Cancelled, reason);
    }

    // 메시지 삭제


    // 읽음 처리
    @Transactional
    public void markMessagesAsRead(Long currentUserId, Long roomId) {
        User currentUser = userService.getUserById(currentUserId);

        // 상대방이 읽지 않은 채팅 내역 가져오기
        Room room = roomRepository.findById(roomId).orElseThrow();
        List<Chat> unreadMessages = chatRepository.findByRoomAndUserIsNotAndIsReadFalse(room, currentUser);

        // 읽음 처리
        unreadMessages.forEach(chat -> chat.updateIsRead(true));
        chatRepository.saveAll(unreadMessages);

        // 안 읽은 알람 개수 초기화
        String unreadCountKey = "chat:unread:" + roomId + ":" + currentUserId;
        redisUtils.deleteData(unreadCountKey);

        // 클라이언트에게 접속한 유저가 읽었음을 알림
        simpMessagingTemplate.convertAndSend("/topic/room/" + roomId + "/read", currentUser.getId());
    }
}
