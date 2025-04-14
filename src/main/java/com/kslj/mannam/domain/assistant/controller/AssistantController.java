package com.kslj.mannam.domain.assistant.controller;

import com.kslj.mannam.domain.assistant.service.AssistantService;
import com.kslj.mannam.domain.user.dto.UserSignUpRequestDto;
import com.kslj.mannam.domain.user.enums.Gender;
import com.kslj.mannam.domain.user.enums.SocialType;
import com.kslj.mannam.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RequiredArgsConstructor
@Controller
public class AssistantController {

    private final AssistantService assistantService;
    private final UserService userService;

    @PostMapping("/assistant/test")
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
        long id = assistantService.createQuestionAndSendToAI(userService.getUserById(userId), content);
        return ResponseEntity.ok(id);
    }
}
