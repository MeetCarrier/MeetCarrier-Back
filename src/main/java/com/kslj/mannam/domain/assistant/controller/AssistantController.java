package com.kslj.mannam.domain.assistant.controller;

import com.kslj.mannam.domain.assistant.dto.AssistantDataDto;
import com.kslj.mannam.domain.assistant.dto.AssistantQuestionDto;
import com.kslj.mannam.domain.assistant.dto.AssistantResponseDto;
import com.kslj.mannam.domain.assistant.service.AssistantService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Controller
@Tag(name = "AI 비서", description = "사용자가 AI 비서에게 보낸 질문과 답변 관리 API")
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantService assistantService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/test")
    public ResponseEntity<?> createQuestion(@RequestParam("content") String content) {
        User user = userService.getUserById(1000);
        assistantService.createQuestionAndSendToAI(user, content);
        return ResponseEntity.ok(assistantService.getQuestionsAndAnswers(user));
    }

    @MessageMapping("/api/assistant/send")
    public void sendQuestion(Principal principal, @AuthenticationPrincipal UserDetailsImpl userDetails, AssistantQuestionDto dto) {
        System.out.println("principal = " + principal);

        User sender = userDetails.getUser();

        log.info("질문 수신: userId={}, content={}", sender.getId(), dto.getContent());

        // 질문 저장
        assistantService.createQuestionAndSendToAI(sender, dto.getContent());

        AssistantDataDto questionDto = AssistantDataDto.builder()
                .content(dto.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        // AI 비서 채팅방에 표시
        messagingTemplate.convertAndSend("/topic/assistant/" + sender.getId(), questionDto);
    }

    @GetMapping
    @Operation(
            summary     = "질문 및 답변 조회",
            description = "현재 로그인한 사용자가 주고받은 질문과 답변 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "조회 성공",
                            content      = @Content(
                                    mediaType = "application/json",
                                    schema    = @Schema(implementation = AssistantResponseDto.class)
                            )
                    )
            }
    )
    public ResponseEntity<?> getAssistantQuestionsAndAnswers(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        AssistantResponseDto questionsAndAnswers = assistantService.getQuestionsAndAnswers(userDetails.getUser());

        return ResponseEntity.ok(questionsAndAnswers);
    }
}
