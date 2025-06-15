package com.kslj.mannam.domain.chat.service;

import com.kslj.mannam.domain.chat.dto.ChatMessageDto;
import com.kslj.mannam.domain.chat.dto.ChatResponseDto;
import com.kslj.mannam.domain.chat.dto.ChatbotMessageDto;
import com.kslj.mannam.domain.chat.entity.Chat;
import com.kslj.mannam.domain.chat.enums.MessageType;
import com.kslj.mannam.domain.chat.repository.ChatRepository;
import com.kslj.mannam.domain.room.entity.Room;
import com.kslj.mannam.domain.room.repository.RoomRepository;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatRepository chatRepository;
    private final RoomRepository roomRepository;
    private final RabbitTemplate rabbitTemplate;
    private final UserService userService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    // 유저 질의 저장 및 챗봇에게 전달
    @Transactional
    public void saveQuery(ChatbotMessageDto dto, long roomId, User user) {
        Room room = roomRepository.findById(roomId).orElseThrow();

        // 질의 저장
        ChatMessageDto queryMessageDto = ChatMessageDto.builder()
                .type(MessageType.CHATBOT)
                .message(dto.getMessage())
                .roomId(roomId)
                .isVisible(dto.getIsVisible())
                .build();
        ChatResponseDto queryResponse = chatService.saveChatMessage(queryMessageDto, roomId, user, false);

        // 채팅방에 브르드캐스트
        log.info("isChatbot = {}", queryResponse.isChatbot());
        messagingTemplate.convertAndSend("/topic/room/" + roomId, queryResponse);

        // 메시지 구성 및 RabbitMQ 전송
        List<String> chatList = chatRepository.findChatByRoomAndType(room, MessageType.TEXT).stream().map(Chat::getMessage).toList();
        String chats = String.join("\n", chatList);

        Map<String, Object> message = new HashMap<>();
        message.put("chats", chats);
        message.put("userId", user.getId());
        message.put("roomId", roomId);
        message.put("isVisible", dto.getIsVisible());
        message.put("question", dto.getMessage());

        rabbitTemplate.convertAndSend("chatbot_request_queue", message);
    }

    // 챗봇 답변 저장 및 반환
    @Transactional
    @RabbitListener(queues = "chatbot_response_queue")
    public void receiveResponse(Map<String, Object> response) {
        try {
            // 데이터 받아와서 DB에 저장
            long userId = ((Number) response.get("userId")).longValue();
            long roomId = ((Number) response.get("roomId")).longValue();
            String answer = (String) response.get("answer");
            Boolean isVisible = (Boolean) response.get("isVisible");

            log.info("answer: {}", answer);
            User user = userService.getUserById(userId);

            ChatMessageDto dto = ChatMessageDto.builder()
                    .type(MessageType.CHATBOT)
                    .message(answer)
                    .roomId(roomId)
                    .isVisible(isVisible)
                    .build();

            // 응답 내용 받아와서 DB에 저장
            ChatResponseDto responseDto = chatService.saveChatMessage(dto, roomId, user, true);

            // 채팅방에 브로드캐스트
            messagingTemplate.convertAndSend("/topic/room/" + roomId, responseDto);
        } catch (Exception e) {
            log.error("Failed to process Chatbot response", e);
        }
    }
}
