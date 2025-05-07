package com.kslj.mannam.domain.survey.controller;

import com.kslj.mannam.domain.survey.dto.SurveyAnswerRequestDto;
import com.kslj.mannam.domain.survey.dto.SurveyAnswerResponseDto;
import com.kslj.mannam.domain.survey.dto.SurveyLeaveDto;
import com.kslj.mannam.domain.survey.dto.SurveyQuestionResponseDto;
import com.kslj.mannam.domain.survey.service.SurveyService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/survey")
public class SurveyController {

    private final SurveyService surveyService;
    private final UserService userService;

    // 설문 세션에 사용할 질문 생성
    @PostMapping("/{sessionId}/questions")
    public ResponseEntity<?> createSurveyQuestion(
            @PathVariable(value = "sessionId") long sessionId
    ) {
        surveyService.createSurveyQuestions(sessionId);
        return ResponseEntity.ok().build();
    }

    // 설문 질문 조회
    @GetMapping("/{sessionId}/questions")
    public ResponseEntity<List<SurveyQuestionResponseDto>> getSurveyQuestions(
            @PathVariable(value = "sessionId") long sessionId
    ) {
        List<SurveyQuestionResponseDto> questions = surveyService.getSurveyQuestions(sessionId);
        return ResponseEntity.ok(questions);
    }

    // 설문 답변 조회
    @GetMapping("/{sessionId}/answers")
    public ResponseEntity<List<SurveyAnswerResponseDto>> getSurveyAnswers(
            @PathVariable(value = "sessionId") long sessionId
    ) {
        List<SurveyAnswerResponseDto> answers = surveyService.getSurveyAnswers(sessionId);
        return ResponseEntity.ok(answers);
    }

    // 설문 답변 등록
    @PostMapping("/{sessionId}/answers/{userId}")
    public ResponseEntity<?> submitSurveyAnswer(
            @PathVariable(value = "sessionId") long sessionId,
            @PathVariable(value = "userId") long userId,
            @RequestBody SurveyAnswerRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        // User user = userDetails.getUser();
        User user = userService.getUserById(userId);
        surveyService.submitSurveyAnswer(sessionId, requestDto, user);
        return ResponseEntity.ok().build();
    }

    @MessageMapping("/survey/leave")
    public void surveyLeave(SurveyLeaveDto dto) {
        User user = userService.getUserById(dto.getLeaverId());
        surveyService.leaveSession(dto.getSessionId(), user);
    }
}
