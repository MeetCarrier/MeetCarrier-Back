package com.kslj.mannam.domain.match.controller;

import com.kslj.mannam.domain.match.dto.MatchRequestResponseDto;
import com.kslj.mannam.domain.match.service.MatchRequestService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/matches")
@Tag(name="매칭 요청 전송 API", description="만남 요청을 전송, 요청을 수락, 거절하는 API")
public class MatchRequestController {

    private final MatchRequestService matchRequestService;

    @Operation(
            summary = "매칭 요청 전송",
            description = "특정 유저에게 매칭 요청을 보냅니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매칭 요청 성공"),
            @ApiResponse(responseCode = "400", description = "요청 실패")
    })
    @PostMapping("/request")
    public ResponseEntity<?> sendMatchRequest(
            @Parameter(description = "매칭 요청 받을 유저 ID", required = true)
            @RequestParam("receiverId") long receiverId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        long senderId = userDetails.getUser().getId();
        matchRequestService.createMatchRequest(senderId, receiverId);
        return ResponseEntity.ok("매칭 요청을 전송했습니다.");
    }

    @Operation(
            summary = "매칭 요청 응답",
            description = "받은 매칭 요청을 수락 또는 거절합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "응답 처리 완료",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PostMapping("/respond")
    public ResponseEntity<?> respondToMatchRequest(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "매칭 응답 DTO (요청 ID, 수락 여부)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = MatchRequestResponseDto.class))
            )
            @RequestBody MatchRequestResponseDto dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        boolean matched = matchRequestService.processRespond(userDetails.getId(), dto.getRequestId(), dto.isAccepted());

        if (matched) {
            return ResponseEntity.ok("매칭 수락");
        } else {
            return ResponseEntity.ok("매칭 거절");
        }
    }
}