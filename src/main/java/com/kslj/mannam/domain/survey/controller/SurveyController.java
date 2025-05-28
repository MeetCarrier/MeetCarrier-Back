package com.kslj.mannam.domain.survey.controller;

import com.kslj.mannam.domain.survey.dto.SurveyAnswerRequestDto;
import com.kslj.mannam.domain.survey.dto.SurveyAnswerResponseDto;
import com.kslj.mannam.domain.survey.dto.SurveyLeaveDto;
import com.kslj.mannam.domain.survey.dto.SurveyQuestionResponseDto;
import com.kslj.mannam.domain.survey.service.SurveyService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/survey")
@Tag(name = "설문", description = "설문 관리 API")
public class SurveyController {

    private final SurveyService surveyService;

    // 설문 질문 조회
    @Operation(
            summary     = "설문 질문 조회",
            description = "지정된 세션(sessionId)의 설문 질문 목록을 조회합니다.",
            parameters = {
                    @Parameter(
                            name        = "sessionId",
                            description = "조회할 설문 세션의 ID",
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
                                            schema = @Schema(implementation = SurveyQuestionResponseDto.class)
                                    )
                            )
                    )
            }
    )
    @GetMapping("/{sessionId}/questions")
    public ResponseEntity<List<SurveyQuestionResponseDto>> getSurveyQuestions(
            @PathVariable("sessionId") long sessionId) {
        List<SurveyQuestionResponseDto> questions =
                surveyService.getSurveyQuestions(sessionId);
        return ResponseEntity.ok(questions);
    }

    // 설문 답변 조회
    @Operation(
            summary     = "설문 답변 조회",
            description = "지정된 세션(sessionId)의 설문 답변 목록을 조회합니다.",
            parameters = {
                    @Parameter(
                            name        = "sessionId",
                            description = "조회할 설문 세션의 ID",
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
                                            schema = @Schema(implementation = SurveyAnswerResponseDto.class)
                                    )
                            )
                    )
            }
    )
    @GetMapping("/{sessionId}/answers")
    public ResponseEntity<List<SurveyAnswerResponseDto>> getSurveyAnswers(
            @PathVariable("sessionId") long sessionId) {
        List<SurveyAnswerResponseDto> answers =
                surveyService.getSurveyAnswers(sessionId);
        return ResponseEntity.ok(answers);
    }

    // 설문 답변 등록
    @Operation(
            summary     = "설문 답변 등록",
            description = "지정된 세션(sessionId)에 대해 유저가 설문 답변을 제출합니다.",
            parameters = {
                    @Parameter(
                            name        = "sessionId",
                            description = "제출할 설문 세션의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "설문 답변 요청 DTO",
                    required    = true,
                    content     = @Content(
                            mediaType = "application/json",
                            array        = @ArraySchema(
                                    schema = @Schema(implementation = SurveyAnswerRequestDto.class)
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "등록 성공"
                    )
            }
    )
    @PostMapping("/{sessionId}/{userId}/answers")
    public ResponseEntity<Void> submitSurveyAnswer(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("sessionId") long sessionId,
            @RequestBody List<SurveyAnswerRequestDto> answers, @PathVariable String userId) {
        User user = userDetails.getUser();
        surveyService.submitSurveyAnswer(sessionId, answers, user);
        return ResponseEntity.ok().build();
    }

    // WebSocket용 @MessageMapping (Swagger 문서화 제외)
    @MessageMapping("/api/survey/leave")
    public void surveyLeave(SimpMessageHeaderAccessor headerAccessor, @Payload SurveyLeaveDto dto) {

        Authentication authentication = (Authentication) headerAccessor.getUser();
        if (authentication == null) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        surveyService.leaveSession(dto.getSessionId(), user);
    }

    @PostMapping("/{matchId}/{sessionId}/questions")
    public ResponseEntity<List<SurveyQuestionResponseDto>> testCreateQuestions(
            @PathVariable("matchId") long matchId,
            @PathVariable("sessionId") long sessionId
    ) {
        surveyService.createSurveyQuestions(matchId, sessionId);
        List<SurveyQuestionResponseDto> questions = surveyService.getSurveyQuestions(sessionId);

        return ResponseEntity.ok(questions);
    }
}
