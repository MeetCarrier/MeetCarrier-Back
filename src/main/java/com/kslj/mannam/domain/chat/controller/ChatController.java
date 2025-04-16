package com.kslj.mannam.domain.chat.controller;

import com.kslj.mannam.domain.chat.dto.ChatMessageDto;
import com.kslj.mannam.domain.chat.dto.ChatResponseDto;
import com.kslj.mannam.domain.chat.service.ChatService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    @MessageMapping("/chat/send")
    public void sendMessage(ChatMessageDto dto) throws Exception {
        long roomId = dto.getRoomId();

        // 임시로 user 설정. 이후, UserDetailsImpl을 이용하도록 변경 필요.
        User sender = userService.getUserById(1);

        if(!chatService.inspectUser(roomId, sender)){
            throw new AccessDeniedException("해당 채팅방 참여자가 아닙니다.");
        }

        log.info("채팅 수신: roomId={}, message={}", roomId, dto.getMessage());

        // 메시지 저장
        chatService.saveChatMessage(dto, roomId, sender);

        ChatResponseDto response = dto.toChatResponseDto(sender);
        log.info("채팅 브로드캐스트: roomId={}, response={}", roomId, response);

        // 채팅방 유저들에게 브로드캐스트
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId, response);
    }

    @GetMapping("/chat/{roomId}")
    public ResponseEntity<?> getChatMessages(@PathVariable(value="roomId") long roomId) throws Exception {
        List<ChatResponseDto> chatMessages = chatService.getChatMessages(roomId);

        return ResponseEntity.ok(chatMessages);
    }
}
