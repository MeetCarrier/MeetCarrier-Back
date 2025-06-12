package com.kslj.mannam.domain.match.controller;

import com.kslj.mannam.domain.match.dto.*;
import com.kslj.mannam.domain.match.enums.MatchStatus;
import com.kslj.mannam.domain.match.service.MatchQueueManager;
import com.kslj.mannam.domain.match.service.MatchService;
import com.kslj.mannam.domain.review.dto.ReviewQueueDto;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.enums.Gender;
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
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/matches")
@Tag(name = "매칭", description = "사용자 매칭 관리 API")
public class MatchController {

    private final MatchService matchService;
    private final MatchQueueManager matchQueueManager;

    // 현재 유저 매칭 목록 조회
    @Operation(
            summary     = "매칭 목록 조회",
            description = "로그인한 사용자의 매칭 목록을 조회합니다.<br><br>" +
                    "매칭 상태 설명:<br>" +
                    "- Matched: 매칭 완료, 설문 진행 전(백엔드 전용)<br>" +
                    "- Surveying: 설문 단계 진행 중<br>" +
                    "- Chatting: 채팅 중<br>" +
                    "- Meeting: 오프라인 만남 진행 중<br>" +
                    "- Reviewing: 만남 이후 리뷰 작성 가능<br>" +
                    "- Completed: 모든 절차 완료<br>" +
                    "- Survey_Cancelled: 설문 단계에서 취소<br>" +
                    "- Chat_Cancelled: 채팅 단계에서 취소",
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
    @GetMapping()
    public ResponseEntity<?> getMatches(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<MatchResponseDto> matches = matchService.getMatches(userDetails.getUser());

        return ResponseEntity.ok(matches);
    }

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

        matchService.updateMatchStatus(matchId, matchStatus);
        return ResponseEntity.ok("상태가 업데이트되었습니다. matchId = " + matchId);
    }

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

    @Operation(
            summary = "매칭 요청 취소",
            description = "현재 매칭 큐에 들어가 있는 유저의 요청을 취소합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "취소 성공 또는 이미 매칭되지 않음")
            }
    )
    @DeleteMapping("/cancel")
    public ResponseEntity<String> cancelMatching(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        boolean cancelled = matchQueueManager.cancelMatching(user.getId());

        if (cancelled) {
            return ResponseEntity.ok("매칭 요청이 취소되었습니다.");
        } else {
            return ResponseEntity.ok("매칭 큐에 등록된 유저가 아니거나 이미 매칭이 완료되었습니다.");
        }
    }

    @Operation(
            summary = "새 매칭 요청 가능 여부 조회",
            description = "현재 로그인한 사용자가 새로운 매칭을 요청할 수 있는 상태인지 확인합니다.<br>" +
                    "진행 중인 매칭(Surveying, Chatting, Meeting 상태)이 있으면 false를, 없으면 true를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "boolean", description = "새 매칭 요청 가능하면 true, 불가능하면 false")
                            )
                    )
            }
    )
    @GetMapping("/can-request")
    public ResponseEntity<Boolean> canRequestNewMatch(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        // 현재 로그인한 사용자 정보 가져오기
        User currentUser = userDetails.getUser();

        // 매칭 가능 여부 조회
        boolean canRequest = matchService.canRequestNewMatch(currentUser);

        return ResponseEntity.ok(canRequest);
    }

    // 취소 사유 저장
    @PostMapping("/{matchId}/cancelReason")


    // 매칭 요청 전송
    @MessageMapping("/api/start-matching")
    public void startMatching(SimpMessageHeaderAccessor headerAccessor) {

        Authentication authentication = (Authentication) headerAccessor.getUser();
        if (authentication == null) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();

        matchQueueManager.registerUserSession(user.getId());  // 세션 등록
        matchQueueManager.addNewUser(user);    // RabbitMQ로 요청 전송
    }

    // 대기 유저 추가 (테스트용)
    @PostMapping("/add-waiting-user")
    public ResponseEntity<String> addWaitingUser() {
        MatchQueueRequestDto waitingUser1 = MatchQueueRequestDto.builder()
                .userId(1L)
                .age(23L)
                .gender(Gender.Male)
                .latitude(35.8722)
                .longitude(128.6025)
                .phone("010-6666-1234")
                .interests("축구,야구,콘솔,노래방,그림 그리기")
                .depressionScore(5)
                .efficacyScore(15)
                .relationshipScore(10)
                .reviews(new ArrayList<>())
                .build();

        matchQueueManager.addWaitingUserDirectly(waitingUser1);

        MatchQueueRequestDto waitingUser2 = MatchQueueRequestDto.builder()
                .userId(2L)
                .age(28L)
                .gender(Gender.Male)
                .latitude(35.8722)
                .longitude(128.6025)
                .phone("010-5678-5678")
                .interests("보드게임,모바일,콘솔,축구,웹툰")
                .depressionScore(10)
                .efficacyScore(20)
                .relationshipScore(15)
                .reviews(List.of(
                        new ReviewQueueDto(4, 1L, 3),
                        new ReviewQueueDto(4, 2L, 2),
                        new ReviewQueueDto(4, 3L, 1)
                ))
                .build();

        matchQueueManager.addWaitingUserDirectly(waitingUser2);

        MatchQueueRequestDto waitingUser3 = MatchQueueRequestDto.builder()
                .userId(3L)
                .age(22L)
                .gender(Gender.Male)
                .latitude(36.3504)
                .longitude(127.3845)
                .phone("010-0000-0000")
                .interests("뮤지컬,재즈,콘서트,전시회,야구")
                .depressionScore(5)
                .efficacyScore(15)
                .relationshipScore(15)
                .reviews(List.of(
                        new ReviewQueueDto(3, 1L, 3)
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
