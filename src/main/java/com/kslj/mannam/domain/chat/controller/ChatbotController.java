package com.kslj.mannam.domain.chat.controller;

import com.kslj.mannam.domain.chat.dto.ChatMessageDto;
import com.kslj.mannam.domain.chat.service.ChatService;
import com.kslj.mannam.domain.chat.service.ChatbotService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.nio.file.AccessDeniedException;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final ChatService chatService;
    private final UserService userService;

    @MessageMapping("/api/chatbot/send")
    public void chatbotSend(@RequestBody ChatMessageDto dto) throws Exception {
        long roomId = dto.getRoomId();

        // 임시로 user 설정. 차후에 변경
        User sender = userService.getUserById(dto.getUserId());

        if(!chatService.inspectUser(roomId, sender)){
            throw new AccessDeniedException("해당 채팅방 참여자가 아닙니다.");
        }

        chatbotService.saveQuery(dto, roomId, sender);
    }
}
