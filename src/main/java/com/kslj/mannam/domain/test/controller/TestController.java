package com.kslj.mannam.domain.test.controller;

import com.kslj.mannam.domain.test.dto.TestRequestDto;
import com.kslj.mannam.domain.test.dto.TestResponseDto;
import com.kslj.mannam.domain.test.service.TestService;
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
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Tag(name = "자가진단 테스트", description = "자가진단 테스트 관리 API")
@Controller
@RequestMapping("/test")
class TestController {

    private final TestService testService;
    private final UserService userService;

    // 현재 유저의 테스트 결과 목록 반환
    @GetMapping
    @Operation(
            summary = "자가진단 테스트 결과 조회",
            description = "현재 로그인한 유저의 자가진단 테스트 결과를 조회합니다. 최신순으로 최대 10개까지 가져옵니다.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = TestResponseDto.class)
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<?> getTestList(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<TestResponseDto> tests = testService.getTestByUserId(userService.getUserById(1));

        return ResponseEntity.ok(tests);
    }

    // 새로운 테스트 결과 추가
    @PostMapping("/register")
    @Operation(
            summary = "새로운 자가진단 테스트 결과 추가",
            description = "유저가 실시한 새로운 자가진단 테스트 결과를 저장합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "테스트 결과 요청 DTO",
                    required    = true,
                    content     = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(implementation = TestRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "저장 성공",
                            content     = @Content(
                                    mediaType = "text/plain",
                                    schema    = @Schema(type = "string")
                            )
                    )
            }
    )
    public ResponseEntity<?> createTest(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @RequestBody TestRequestDto requestDto) {
        long savedTestId = testService.createTest(requestDto, userService.getUserById(1));

        return ResponseEntity.ok("테스트 결과가 추가되었습니다. TestId = " + savedTestId);
    }

    // 테스트 결과 삭제
    @DeleteMapping("/{testId}")
    @Operation(
            summary     = "자가진단 테스트 결과 삭제",
            description = "지정된 ID의 자가진단 테스트 결과를 삭제합니다.",
            parameters  = {
                    @Parameter(
                            name        = "testId",
                            description = "삭제할 테스트 결과의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "삭제 성공",
                            content      = @Content(
                                    mediaType = "text/plain",
                                    schema    = @Schema(type = "string")
                            )
                    )
            }
    )
    public ResponseEntity<?> deleteTest(@PathVariable("testId") Long testId) {
        long deletedTestId = testService.deleteTestByTestId(testId);

        return ResponseEntity.ok("테스트 결과가 삭제되었습니다. TestId = " + deletedTestId);
    }
}
