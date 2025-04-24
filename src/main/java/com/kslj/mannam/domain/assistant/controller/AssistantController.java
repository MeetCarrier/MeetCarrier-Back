package com.kslj.mannam.domain.assistant.controller;

import com.kslj.mannam.domain.assistant.dto.AssistantDto;
import com.kslj.mannam.domain.assistant.dto.AssistantResponseDto;
import com.kslj.mannam.domain.assistant.service.AssistantService;
import com.kslj.mannam.domain.user.dto.UserSignUpRequestDto;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.enums.Gender;
import com.kslj.mannam.domain.user.enums.SocialType;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/assistant")
public class AssistantController {

    private final AssistantService assistantService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/test")
    public ResponseEntity<?> createQuestion(@RequestParam(name="content") String content) {
        long userId = userService.createUser(UserSignUpRequestDto.builder()
                .region("서울")
                .socialType(SocialType.Google)
                .nickname("테스트유저")
                .gender(Gender.Male)
                .personalities("소심")
                .interests("게임")
                .preferences("축구")
                .socialId("1234")
                .build());
        long id = assistantService.createQuestionAndSendToAI(userService.getUserById(userId), content).getId();
        return ResponseEntity.ok(id);
    }

    @MessageMapping("/send")
    public void sendQuestion(@RequestParam(name="content") String content) {

        // 임시로 user 설정. UserDetailsImpl을 이용하도록 변경 예정
        User sender = userService.getUserById(1);

        log.info("질문 수신: userId={}, content={}", sender.getId(), content);

        // 질문 저장
        assistantService.createQuestionAndSendToAI(sender, content);

        AssistantDto questionDto = AssistantDto.builder()
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        // AI 비서 채팅방에 표시
        messagingTemplate.convertAndSend("/topic/assistant/" + sender.getId(), questionDto);
    }

    @GetMapping
    public ResponseEntity<?> getAssistantQuestionsAndAnswers(@AuthenticationPrincipal UserDetailsImpl user) {
        AssistantResponseDto questionsAndAnswers = assistantService.getQuestionsAndAnswers(userService.getUserById(1));

        return ResponseEntity.ok(questionsAndAnswers);
    }
}
