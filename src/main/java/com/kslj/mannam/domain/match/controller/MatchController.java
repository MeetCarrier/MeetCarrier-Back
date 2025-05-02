package com.kslj.mannam.domain.match.controller;

import com.kslj.mannam.domain.match.dto.*;
import com.kslj.mannam.domain.match.enums.MatchStatus;
import com.kslj.mannam.domain.match.service.MatchQueueManager;
import com.kslj.mannam.domain.match.service.MatchService;
import com.kslj.mannam.domain.review.dto.ReviewQueueDto;
import com.kslj.mannam.domain.test.dto.TestRequestDto;
import com.kslj.mannam.domain.test.service.TestService;
import com.kslj.mannam.domain.user.dto.UserSignUpRequestDto;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.enums.Gender;
import com.kslj.mannam.domain.user.enums.SocialType;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/matches")
@Tag(name = "매칭", description = "사용자 매칭 관리 API")
public class MatchController {

    private final MatchService matchService;
    private final UserService userService;
    private final MatchQueueManager matchQueueManager;
    private final TestService testService;

    // 현재 유저 매칭 목록 조회
    @Operation(
            summary     = "매칭 목록 조회",
            description = "로그인한 사용자의 매칭 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "조회 성공",
                            content      = @Content(
                                    mediaType = "application/json",
                                    array     = @ArraySchema(
                                            schema = @Schema(implementation = MatchResponseDto.class)
                                    )
                            )
                    )
            }
    )
    @GetMapping
    public ResponseEntity<?> getMatches(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<MatchResponseDto> matches = matchService.getMatches(userDetails.getUser());

        return ResponseEntity.ok(matches);
    }

    // 매칭 상태 업데이트
    // 매칭 상태 업데이트
    @Operation(
            summary     = "매칭 상태 업데이트",
            description = "지정된 매칭의 상태를 변경합니다.",
            parameters = {
                    @Parameter(
                            name        = "matchId",
                            description = "업데이트할 매칭의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "상태 변경 정보",
                    required    = true,
                    content     = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(implementation = StatusUpdateDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "업데이트 성공",
                            content      = @Content(
                                    mediaType = "text/plain",
                                    schema    = @Schema(type = "string"),
                                    examples  = @ExampleObject(value = "상태가 업데이트되었습니다. matchId = 123")
                            )
                    )
            }
    )
    @PatchMapping("/{matchId}")
    public ResponseEntity<?> updateStatus(@PathVariable("matchId") long matchId,
                                          @RequestBody StatusUpdateDto dto) {
        MatchStatus matchStatus = dto.getMatchStatus();

        matchService.updateMathStatus(matchId, matchStatus);
        return ResponseEntity.ok("상태가 업데이트되었습니다. matchId = " + matchId);
    }

    // 매칭 삭제
    // 매칭 삭제
    @Operation(
            summary     = "매칭 삭제",
            description = "지정된 매칭을 삭제합니다.",
            parameters = {
                    @Parameter(
                            name        = "matchId",
                            description = "삭제할 매칭의 ID",
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
                                    schema    = @Schema(type = "string"),
                                    examples  = @ExampleObject(value = "매칭 정보가 삭제되었습니다. deletedMatchId = 123")
                            )
                    )
            }
    )
    @DeleteMapping("/{matchId}")
    public ResponseEntity<?> deleteMatch(@PathVariable("matchId") long matchId) {
        long deletedMatchId = matchService.deleteMatch(matchId);

        return ResponseEntity.ok("매칭 정보가 삭제되었습니다. deletedMatchId = " + deletedMatchId);
    }

    // 매칭 요청 전송
    @MessageMapping("/start-matching")
    public void startMatching() {
        // 차후 userDetailsImpl을 이용하도록 코드 변경 필요
        UserSignUpRequestDto userSignUpRequestDto = UserSignUpRequestDto.builder()
                .socialId("12341234")
                .socialType(SocialType.Google)
                .nickname("테스트유저1")
                .gender(Gender.Female)
                .region("Daegu")
                .age(25L)
                .personalities("소심,느긋")
                .preferences("sports,gaming")
                .interests("sports,gaming")
                .build();

        long userId = userService.createUser(userSignUpRequestDto);
        User user = userService.getUserById(userId);

        TestRequestDto testRequestDto = TestRequestDto.builder()
                .depressionScore(60)
                .efficacyScore(40)
                .relationshipScore(60)
                .build();
        testService.createTest(testRequestDto, user);

        // long userId = userDetails.getUser().getId();
        matchQueueManager.registerUserSession(userId);  // 세션 등록
        matchQueueManager.addNewUser(user);    // RabbitMQ로 요청 전송
    }

    // 대기 유저 추가 (테스트용)
    @PostMapping("/add-waiting-user")
    public ResponseEntity<String> addWaitingUser() {
        MatchQueueRequestDto waitingUser1 = MatchQueueRequestDto.builder()
                .userId(1L)
                .region("Busan")
                .age(23L)
                .interests("reading,sports")
                .depressionScore(65)
                .efficacyScore(40)
                .relationshipScore(50)
                .reviews(new ArrayList<>())
                .build();

        matchQueueManager.addWaitingUserDirectly(waitingUser1);

        MatchQueueRequestDto waitingUser2 = MatchQueueRequestDto.builder()
                .userId(2L)
                .region("Seoul")
                .age(28L)
                .interests("music,travel,gaming")
                .depressionScore(78)
                .efficacyScore(66)
                .relationshipScore(56)
                .reviews(List.of(
                        new ReviewQueueDto(4, 1L),
                        new ReviewQueueDto(4, 3L)
                ))
                .build();

        matchQueueManager.addWaitingUserDirectly(waitingUser2);

        MatchQueueRequestDto waitingUser3 = MatchQueueRequestDto.builder()
                .userId(3L)
                .region("Daegu")
                .age(17L)
                .interests("gaming")
                .depressionScore(44)
                .efficacyScore(53)
                .relationshipScore(12)
                .reviews(List.of(
                        new ReviewQueueDto(3, 1L)
                ))
                .build();

        matchQueueManager.addWaitingUserDirectly(waitingUser3);

        return ResponseEntity.ok("대기 유저 추가 완료");
    }

    // 새 매칭 유저 추가 (테스트용)
//    @PostMapping("/start-matching-test")
//    public ResponseEntity<?> startMatching() {
//        MatchQueueRequestDto testUser = MatchQueueRequestDto.builder()
//                .userId(100L)
//                .region("Daegu")
//                .interests("gaming,sports")
//                .depressionScore(27)
//                .efficacyScore(43)
//                .relationshipScore(87)
//                .reviews(List.of(
//                        new ReviewQueueDto(3, 1L)
//                ))
//                .build();
//
//        matchQueueManager.addNewUser(testUser);
//
//        return ResponseEntity.ok("매칭 요청 전송 완료");
//    }

    // 추가: 현재 대기 큐 상태 조회
    @GetMapping("/queue-status")
    public ResponseEntity<List<WaitingUserInfoDto>> getQueueStatus() {
        List<WaitingUserInfoDto> queueStatus = matchQueueManager.getWaitingUsers();
        return ResponseEntity.ok(queueStatus);
    }
}
