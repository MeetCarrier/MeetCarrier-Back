package com.kslj.mannam.domain.invitation.controller;

import com.kslj.mannam.domain.invitation.dto.InvitationRequestDto;
import com.kslj.mannam.domain.invitation.dto.InvitationResponseDto;
import com.kslj.mannam.domain.invitation.dto.RespondToInvitationDto;
import com.kslj.mannam.domain.invitation.service.InvitationService;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/invitation")
@RequiredArgsConstructor
@Tag(name="만남 초대장 API", description="사용자들이 만남 초대장을 전송하고 동의 혹은 거절 API")
public class InvitationController {

    private final InvitationService invitationService;
    private final UserService userService;

    @Operation(summary = "만남 초대장 전송", description = "matchId와 receiverId를 포함한 초대장을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "초대장 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "409", description = "이미 대기 중인 초대장이 존재함", content = @Content)
    })
    @PostMapping
    public ResponseEntity<?> createInvitation(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "초대장 생성 요청 DTO",
                    required = true,
                    content = @Content(schema = @Schema(implementation = InvitationRequestDto.class),
                            examples = @ExampleObject(value = "{\"matchId\": 1, \"receiverId\": 2, \"message\": \"안녕하세요 만나고 싶어요.\"}"))
            )
            @RequestBody InvitationRequestDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        userService.inspectUserDetails(userDetails);
        long invitationId = invitationService.createInvitation(dto, userDetails.getUser());

        return ResponseEntity.ok("초대장이 전송되었습니다." + invitationId);
    }

    @Operation(summary = "만남 초대장 조회", description = "matchId로 해당 초대장을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "초대장 조회 성공"),
            @ApiResponse(responseCode = "404", description = "초대장이 존재하지 않음", content = @Content)
    })
    @GetMapping("/{matchId}")
    public ResponseEntity<?> getInvitation(
            @Parameter(description = "매칭 ID", required = true, example = "1")
            @PathVariable("matchId") Long matchId
    ) {
        InvitationResponseDto dto = invitationService.getInvitation(matchId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "초대장 수락 또는 거절", description = "수락 또는 거절 여부에 따라 초대장을 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "초대장 응답 처리 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 권한 없음", content = @Content),
            @ApiResponse(responseCode = "409", description = "이미 처리된 초대장", content = @Content)
    })
    @PatchMapping("/respond")
    public ResponseEntity<?> acceptInvitation(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "초대장 응답 DTO",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RespondToInvitationDto.class),
                            examples = @ExampleObject(value = "{\"matchId\": 1, \"accepted\": true}"))
            )
            @RequestBody RespondToInvitationDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        userService.inspectUserDetails(userDetails);
        invitationService.respondToInvitation(dto, userDetails.getUser());

        return ResponseEntity.ok(dto.isAccepted() ? "수락 완료" : "거절 완료");
    }
}