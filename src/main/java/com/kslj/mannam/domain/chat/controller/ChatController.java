package com.kslj.mannam.domain.chat.controller;

import com.kslj.mannam.domain.chat.dto.ChatMessageDto;
import com.kslj.mannam.domain.chat.dto.ChatResponseDto;
import com.kslj.mannam.domain.chat.enums.MessageType;
import com.kslj.mannam.domain.chat.service.ChatService;
import com.kslj.mannam.domain.room.entity.Room;
import com.kslj.mannam.domain.room.enums.RoomStatus;
import com.kslj.mannam.domain.room.service.RoomService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
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
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
    private final RoomService roomService;

    @MessageMapping("/api/chat/send")
    public void sendMessage(SimpMessageHeaderAccessor headerAccessor, @Payload ChatMessageDto dto) throws Exception {
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
    public void leaveRoom(SimpMessageHeaderAccessor headerAccessor, @Payload ChatMessageDto dto) throws Exception {
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

        chatService.leaveRoom(roomId, sender, dto.getMessage());

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

        // 채팅방 비활성화
        Room room = roomService.getRoom(roomId);
        room.updateStatus(RoomStatus.Deactivate);
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

    @GetMapping("/api/chat/test")
    public String test() {
        return "chat-test";
    }
}
