package com.kslj.mannam.domain.chat.service;

import com.kslj.mannam.domain.chat.dto.ChatMessageDto;
import com.kslj.mannam.domain.chat.dto.ChatResponseDto;
import com.kslj.mannam.domain.chat.dto.LastChatDto;
import com.kslj.mannam.domain.chat.entity.Chat;
import com.kslj.mannam.domain.chat.enums.MessageType;
import com.kslj.mannam.domain.chat.repository.ChatRepository;
import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.enums.MatchStatus;
import com.kslj.mannam.domain.room.entity.Room;
import com.kslj.mannam.domain.room.enums.RoomStatus;
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
    private final ChatPresenceService chatPresenceService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    // 메시지 저장
    @Transactional
    public ChatResponseDto saveChatMessage(ChatMessageDto dto, long roomId, User sender) {
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

        // Chat 엔티티 생성 및 저장
        Chat newChat = Chat.builder()
                .type(dto.getType())
                .message(dto.getMessage())
                .imageUrl(dto.getImageUrl())
                .room(room)
                .user(sender)
                .isRead(isReceiverOnline)
                .isVisible(dto.getIsVisible() != null ? dto.getIsVisible() : true)
                .build();

        Chat savedChat = chatRepository.save(newChat);

        // 클라이언트에 보낼 응답 DTO 생성 (채팅방 내부)
        ChatResponseDto responseDto = ChatResponseDto.builder()
                .type(dto.getType())
                .message(dto.getMessage())
                .imageUrl(dto.getImageUrl())
                .sender(sender.getId())
                .isRead(isReceiverOnline)
                .isVisible(savedChat.getIsVisible())
                .build();

        if (savedChat.getIsVisible()) {
            String messageBody;
            if (savedChat.getMessage() == null && savedChat.getImageUrl() != null) {
                messageBody = "사진을 전송했습니다.";
            } else if (savedChat.getType() == MessageType.CHATBOT && savedChat.getMessage() != null) {
                messageBody = "[챗봇] " + savedChat.getMessage();
            } else {
                messageBody = savedChat.getMessage();
            }

            // Redis에 최신 메시지 정보 저장
            LastChatDto lastChatForCache = LastChatDto.builder()
                    .roomId(roomId)
                    .lastMessage(messageBody)
                    .lastMessageAt(savedChat.getSentAt())
                    .build();
            redisUtils.setData("chat:latest:" + roomId, lastChatForCache, 3600 * 24);

            // 메시지 보낸 사람의 채팅 목록도 최신 정보로 업데이트
            LastChatDto lastChatForSender = LastChatDto.builder()
                    .roomId(roomId)
                    .lastMessage(messageBody)
                    .lastMessageAt(savedChat.getSentAt())
                    .unreadCount(0) // 보낸 사람은 항상 읽음 상태
                    .build();
            messagingTemplate.convertAndSend("/topic/user/" + sender.getId() + "/chats", lastChatForSender);

            if (!isReceiverOnline) {
                // Redis에 안 읽은 메시지 개수 증가
                String unreadCountKey = "chat:unread:" + roomId + ":" + receiver.getId();
                Long unreadCount = redisUtils.incrData(unreadCountKey);

                // FCM 푸시 알람 전송
                fcmTokenService.sendPushToUserAsync(
                        receiver,
                        sender.getNickname(),
                        messageBody,
                        "https://www.mannamdeliveries.link",
                        String.valueOf(roomId));

                // 안 읽은 개수를 상대방의 클라이언트에 실시간 전송
                LastChatDto lastChatInfo = LastChatDto.builder()
                        .roomId(roomId)
                        .lastMessage(messageBody)
                        .lastMessageAt(savedChat.getSentAt())
                        .unreadCount(unreadCount.intValue())
                        .build();
                messagingTemplate.convertAndSend("/topic/user/" + receiver.getId() + "/chats", lastChatInfo);
            } else {
                // 최신 메시지 정보 전송
                LastChatDto lastChatForReceiverOnline = LastChatDto.builder()
                        .roomId(roomId)
                        .lastMessage(messageBody)
                        .lastMessageAt(savedChat.getSentAt())
                        .unreadCount(0) // 온라인 상태이므로 안 읽은 개수는 0
                        .build();
                messagingTemplate.convertAndSend("/topic/user/" + receiver.getId() + "/chats", lastChatForReceiverOnline);
            }
        }

        return responseDto;
    }

    // 메시지 전송 (알람 x)
    @Transactional
    public void saveChatMessageWithoutNotification(long matchId, User sender, String message) {

        Room room = roomRepository.getRoomByMatchId(matchId);

        Chat newChat = Chat.builder()
                .type(MessageType.CHATBOT)
                .message(message)
                .room(room)
                .user(sender)
                .build();

        chatRepository.save(newChat);

        ChatMessageDto dto = ChatMessageDto.builder()
                .type(MessageType.CHATBOT)
                .message(message)
                .roomId(room.getId())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/room/" + room.getId(), dto);
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
                    .isVisible(chat.getIsVisible())
                    .isRead(chat.getIsRead())
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

        // 채팅방 비활성화
        room.updateStatus(RoomStatus.Deactivate);
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
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/read", currentUser.getId());
    }

    // 최신 채팅 정보 조회
    @Transactional(readOnly = true)
    public LastChatDto getLastChatInfo(long roomId, long userId) {

        String lastChatKey = "chat:latest:" + roomId;
        LastChatDto lastChatInfo;

        // Redis에서 LastChatDto 객체 조회
        Optional<Object> cachedData = redisUtils.getData(lastChatKey);

        if (cachedData.isPresent()) {
            // 캐시에 저장된 데이터 존재
            lastChatInfo = (LastChatDto) cachedData.get();
        } else {
            // 캐시에 없으면 DB 조회
            Optional<Chat> lastChatOpt = chatRepository.getLastChat(roomId);

            if (lastChatOpt.isPresent()) {
                Chat lastChat = lastChatOpt.get();
                String message = lastChat.getMessage();
                if (message == null && lastChat.getImageUrl() != null) {
                    message = "사진을 보냈습니다.";
                }

                lastChatInfo = LastChatDto.builder()
                        .roomId(roomId)
                        .lastMessage(message)
                        .lastMessageAt(lastChat.getSentAt())
                        .build();

                // Redis에 캐싱
                redisUtils.setData(lastChatKey, lastChatInfo, 3600 * 24);
            } else {
                // DB에도 저장된 채팅 내역이 없으면 빈 DTO 반환
                lastChatInfo = LastChatDto.builder()
                        .roomId(roomId)
                        .build();
            }
        }

        // 사용자별 안 읽은 메시지 개수 처리
        String unreadCountKey = "chat:unread:" + roomId + ":" + userId;
        int unreadCount;

        // Redis에서 안 읽은 메시지 개수 카운트 조회
        Optional<Object> rawUnreadCount = redisUtils.getData(unreadCountKey);
        if (rawUnreadCount.isPresent()) {
            unreadCount = Integer.parseInt(rawUnreadCount.get().toString());
        } else {
            // 없으면 DB 조회
            unreadCount = chatRepository.countUnreadMessages(roomId, userId).intValue();

            // Redis에 캐싱
            redisUtils.setData(unreadCountKey, unreadCount, 3600 * 24);
        }

        // 최종 DTO 생성
        return LastChatDto.builder()
                .roomId(roomId)
                .lastMessage(lastChatInfo.getLastMessage())
                .lastMessageAt(lastChatInfo.getLastMessageAt())
                .unreadCount(unreadCount) // 최종적으로 안 읽은 수 포함
                .build();
    }
}
