package com.kslj.mannam.domain.chat.controller;

import com.kslj.mannam.domain.chat.dto.ChatMessageDto;
import com.kslj.mannam.domain.chat.dto.ChatResponseDto;
import com.kslj.mannam.domain.chat.enums.MessageType;
import com.kslj.mannam.domain.chat.service.ChatService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@Tag(name = "채팅", description = "채팅 메시지 전송 및 조회 API")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    @MessageMapping("/api/chat/send")
    public void sendMessage(ChatMessageDto dto) throws Exception {
        long roomId = dto.getRoomId();

        // 임시로 user 설정. 이후, UserDetailsImpl을 이용하도록 변경 필요.
        User sender = userService.getUserById(dto.getUserId());

        if(!chatService.inspectUser(roomId, sender)){
            throw new AccessDeniedException("해당 채팅방 참여자가 아닙니다.");
        }

        log.info("채팅 수신: roomId={}, message={}", roomId, dto.getMessage());

        // 메시지 저장
        chatService.saveChatMessage(dto, roomId, sender);

        ChatResponseDto response = dto.fromEntity(sender);
        log.info("채팅 브로드캐스트: roomId={}, response={}", roomId, response);

        // 채팅방 유저들에게 브로드캐스트
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId, response);
    }

    @MessageMapping("/api/chat/leave")
    public void leaveRoom(ChatMessageDto dto) throws Exception {
        long roomId = dto.getRoomId();

        // 임시로 user 설정. 차후 UserDetailsImpl을 이용하도록 변경 필요
        User sender = userService.getUserById(1);

        if(!chatService.inspectUser(roomId, sender)){
            throw new AccessDeniedException("해당 채팅방 참여자가 아닙니다.");
        }

        chatService.leaveRoom(roomId, sender);

        // 나감 알림 메시지 생성
        ChatResponseDto leaveNotice = ChatResponseDto.builder()
                .sender(sender.getId())
                .message("상대방이 채팅방을 나갔습니다.")
                .type(MessageType.LEAVE)
                .sentAt(LocalDateTime.now())
                .build();

        // 상대방에게 알림
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId, leaveNotice
        );
    }

    @Operation(
            summary     = "채팅 메시지 조회",
            description = "지정된 채팅방(roomId)의 최근 채팅 메시지들을 조회합니다.",
            parameters = {
                    @Parameter(
                            name        = "roomId",
                            description = "조회할 채팅방의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "조회 성공",
                            content      = @Content(
                                    mediaType = "application/json",
                                    array        = @ArraySchema(
                                            schema = @Schema(implementation = ChatResponseDto.class)
                                    )
                            )
                    )
            }
    )
    @GetMapping("/api/chat/{roomId}")
    public ResponseEntity<?> getChatMessages(@PathVariable("roomId") long roomId) throws Exception {
        List<ChatResponseDto> chatMessages = chatService.getChatMessages(roomId);

        return ResponseEntity.ok(chatMessages);
    }
}
