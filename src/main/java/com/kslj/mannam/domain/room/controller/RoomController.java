package com.kslj.mannam.domain.room.controller;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.service.MatchService;
import com.kslj.mannam.domain.room.service.RoomService;
import com.kslj.mannam.domain.survey.repository.SurveySessionRepository;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/room")
public class RoomController {
    private final MatchService matchService;
    private final RoomService roomService;
    private final SurveySessionRepository surveySessionRepository;
    private final UserService userService;

    @PostMapping("/{matchId}/{userId}/enter")
    @Operation(
            summary     = "채팅방 입장",
            description = "지정된 매칭(matchId)에 대해 유저(userId)를 채팅방에 입장시킵니다. 생성된 sessionId 및 roomId 정보를 반환합니다.",
            parameters = {
                    @Parameter(
                            name        = "matchId",
                            description = "입장할 매칭의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    ),
                    @Parameter(
                            name        = "userId",
                            description = "입장할 유저의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "입장 성공",
                            content      = @Content(
                                    mediaType = "text/plain",
                                    schema    = @Schema(type = "string", example = "sessionId: 10, roomId: 5, userId: 3 입장")
                            )
                    )
            }
    )
    public ResponseEntity<String> enterChatRoom(
            @PathVariable("matchId") Long matchId,
            @PathVariable("userId")  Long userId
    ) {
        User currentUser = userService.getUserById(userId);

        // 1. 유저 입장 여부 저장
        matchService.markUserEnteredChat(matchId, currentUser);

        // 2. 관련 정보 조회
        Long sessionId = surveySessionRepository
                .findSurveySessionByMatchId(matchId)
                .getId();
        long roomId = roomService.getRoomId(matchId);

        String message = String.format(
                "sessionId: %d, roomId: %d, userId: %d 입장",
                sessionId, roomId, userId
        );
        return ResponseEntity.ok(message);
    }

    // 채팅방 상태 조회 -> 입력 활성화 여부 결정
    @Operation(
            summary = "채팅방 상태 조회",
            description = "roomId를 통해 채팅방의 활성화 여부를 조회합니다. isActive가 true면 입력 가능 상태입니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 상태 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 roomId에 대한 채팅방을 찾을 수 없음")
    })
    @GetMapping("/{roomId}")
    public ResponseEntity<?> getRoomStatus(
            @Parameter(description = "채팅방 ID", required = true, example = "123")
            @PathVariable("roomId") long roomId
    ) {
        boolean isActive = roomService.getRoomStatus(roomId);

        Map<String, Object> response = new HashMap<>();
        response.put("roomId", roomId);
        response.put("isActive", isActive);

        return ResponseEntity.ok(response);
    }
}
