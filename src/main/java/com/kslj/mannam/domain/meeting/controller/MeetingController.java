package com.kslj.mannam.domain.meeting.controller;

import com.kslj.mannam.domain.meeting.dto.MeetingRequestDto;
import com.kslj.mannam.domain.meeting.dto.MeetingResponseDto;
import com.kslj.mannam.domain.meeting.service.MeetingService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/meetings")
@Tag(name = "대면 약속", description = "매칭된 사용자 간 대면 약속 관리 API")
public class MeetingController {

    private final MeetingService meetingService;

    // 대면 약속 생성
    @Operation(
            summary     = "대면 약속 요청 생성",
            description = "지정된 matchId로 대면 약속 요창을 생성합니다.",
            parameters = {
                    @Parameter(
                            name        = "matchId",
                            description = "약속을 생성할 매칭 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "대면 약속 요청 생성 요청 DTO",
                    required    = true,
                    content     = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(implementation = MeetingRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description  = "생성 성공",
                            content      = @Content(
                                    mediaType = "application/json",
                                    schema    = @Schema(type = "integer", format = "int64"),
                                    examples  = @ExampleObject(value = "45")
                            )
                    )
            }
    )
    @PostMapping("/{matchId}")
    public ResponseEntity<Long> createMeeting(
            @PathVariable("matchId") long matchId,
            @RequestBody MeetingRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        long meetingId = meetingService.createMeeting(matchId, userDetails.getUser(), requestDto);
        return ResponseEntity.ok(meetingId);
    }

    // 대면 약속 목록 조회
    @Operation(
            summary     = "대면 약속 목록 조회",
            description = "현재 로그인한 사용자가 속한 모든 대면 약속을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "조회 성공",
                            content      = @Content(
                                    mediaType = "application/json",
                                    array     = @ArraySchema(
                                            schema = @Schema(implementation = MeetingResponseDto.class)
                                    )
                            )
                    )
            }
    )
    @GetMapping
    public ResponseEntity<List<MeetingResponseDto>> getMeetings(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<MeetingResponseDto> response = meetingService.getMeetings(userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    // 대면 약속 조회
    @Operation(
            summary     = "대면 약속 조회",
            description = "전달된 matchId를 이용하여 해당 매칭의 저장된 매칭 일정을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content      = @Content(
                                    mediaType = "application/json",
                                    schema    = @Schema(implementation = MeetingResponseDto.class)
                            )
                    )
            }
    )
    @GetMapping("/{matchId}")
    public ResponseEntity<MeetingResponseDto> getMeeting(@PathVariable("matchId") long matchId) {
        MeetingResponseDto response = meetingService.getMeeting(matchId);

        return ResponseEntity.ok(response);
    }

    // 대면 약속 수정
    @Operation(
            summary     = "대면 약속 수정",
            description = "지정된 meetingId의 대면 약속을 수정합니다.\n요청 전송 시 필요한 부분의 데이터만 채워서 보내면 됩니다.",
            parameters = {
                    @Parameter(
                            name        = "meetingId",
                            description = "수정할 대면 약속의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "대면 약속 수정 요청 DTO",
                    required    = true,
                    content     = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(implementation = MeetingRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "수정 성공"
                    )
            }
    )
    @PatchMapping("/{meetingId}")
    public ResponseEntity<Void> updateMeeting(
            @PathVariable("meetingId") long meetingId,
            @RequestBody MeetingRequestDto requestDto
    ) {
        meetingService.updateMeeting(meetingId, requestDto);
        return ResponseEntity.ok().build();
    }

    // 대면 약속 삭제
    @Operation(
            summary     = "대면 약속 삭제",
            description = "지정된 meetingId의 대면 약속을 삭제합니다.",
            parameters = {
                    @Parameter(
                            name        = "meetingId",
                            description = "삭제할 대면 약속의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "삭제 성공"
                    )
            }
    )
    @DeleteMapping("/{meetingId}")
    public ResponseEntity<Void> deleteMeeting(@PathVariable("meetingId") long meetingId) {
        meetingService.deleteMeeting(meetingId);
        return ResponseEntity.ok().build();
    }

    // 대면 약속 요청 수락
    @Operation(
            summary = "대면 약속 수락",
            description = "지정된 meetingId의 약속 제안을 수락합니다.",
            parameters = {
                    @Parameter(name = "meetingId", description = "수락할 대면 약속 ID", required = true, in = ParameterIn.PATH)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "수락 성공")
            }
    )
    @PatchMapping("/{meetingId}/accept")
    public ResponseEntity<?> confirmMeeting(
            @PathVariable("meetingId") long meetingId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        meetingService.confirmMeeting(userDetails.getUser(), meetingId);
        return ResponseEntity.ok().build();
    }

    // 대면 약속 요청 거절
    @Operation(
            summary = "대면 약속 거절",
            description = "지정된 meetingId의 약속 제안을 거절합니다.",
            parameters = {
                    @Parameter(name = "meetingId", description = "거절할 대면 약속 ID", required = true, in = ParameterIn.PATH)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "거절 성공")
            }
    )
    @PatchMapping("/{meetingId}/reject")
    public ResponseEntity<?> rejectMeeting(
            @PathVariable("meetingId") long meetingId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        meetingService.rejectMeeting(userDetails.getUser(), meetingId);
        return ResponseEntity.ok().build();
    }
}
