package com.kslj.mannam.domain.survey.controller;

import com.kslj.mannam.domain.survey.dto.SurveyAnswerRequestDto;
import com.kslj.mannam.domain.survey.dto.SurveyAnswerResponseDto;
import com.kslj.mannam.domain.survey.dto.SurveyLeaveDto;
import com.kslj.mannam.domain.survey.dto.SurveyQuestionResponseDto;
import com.kslj.mannam.domain.survey.service.SurveyService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/survey")
@Tag(name = "설문", description = "설문 관리 API")
public class SurveyController {

    private final SurveyService surveyService;
    private final UserService userService;

    // 설문 세션에 사용할 질문 생성
    @Operation(
            summary     = "설문 질문 생성",
            description = "지정된 세션(sessionId)에 사용할 설문 질문들을 생성합니다.",
            parameters = {
                    @Parameter(
                            name        = "sessionId",
                            description = "질문을 생성할 설문 세션의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "생성 성공"
                    )
            }
    )
    @PostMapping("/{sessionId}/questions")
    public ResponseEntity<Void> createSurveyQuestion(
            @PathVariable long sessionId) {
        surveyService.createSurveyQuestions(sessionId);
        return ResponseEntity.ok().build();
    }

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
            @PathVariable long sessionId) {
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
            @PathVariable long sessionId) {
        List<SurveyAnswerResponseDto> answers =
                surveyService.getSurveyAnswers(sessionId);
        return ResponseEntity.ok(answers);
    }

    // 설문 답변 등록
    @Operation(
            summary     = "설문 답변 등록",
            description = "지정된 세션(sessionId)에 대해 유저(userId)가 설문 답변을 제출합니다.",
            parameters = {
                    @Parameter(
                            name        = "sessionId",
                            description = "제출할 설문 세션의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    ),
                    @Parameter(
                            name        = "userId",
                            description = "답변을 제출하는 유저의 ID",
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
    @PostMapping("/{sessionId}/answers/{userId}")
    public ResponseEntity<Void> submitSurveyAnswer(
            @PathVariable long sessionId,
            @PathVariable long userId,
            @RequestBody List<SurveyAnswerRequestDto> answers) {
        User user = userService.getUserById(userId);
        surveyService.submitSurveyAnswer(sessionId, answers, user);
        return ResponseEntity.ok().build();
    }

    // WebSocket용 @MessageMapping (Swagger 문서화 제외)
    @MessageMapping("/survey/leave")
    public void surveyLeave(SurveyLeaveDto dto) {
        User user = userService.getUserById(dto.getLeaverId());
        surveyService.leaveSession(dto.getSessionId(), user);
    }
}
