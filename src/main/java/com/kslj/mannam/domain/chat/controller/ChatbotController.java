package com.kslj.mannam.domain.chat.controller;

import com.kslj.mannam.domain.chat.dto.ChatMessageDto;
import com.kslj.mannam.domain.chat.service.ChatService;
import com.kslj.mannam.domain.chat.service.ChatbotService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.nio.file.AccessDeniedException;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final ChatService chatService;

    @MessageMapping("/api/chatbot/send")
    public void chatbotSend(SimpMessageHeaderAccessor headerAccessor, @Payload ChatMessageDto dto) throws Exception {
        long roomId = dto.getRoomId();

        Authentication authentication = (Authentication) headerAccessor.getUser();
        if (authentication == null) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User sender = userDetails.getUser();

        if(!chatService.inspectUser(roomId, sender)){
            throw new AccessDeniedException("해당 채팅방 참여자가 아닙니다.");
        }

        chatbotService.saveQuery(dto, roomId, sender);
    }
}
