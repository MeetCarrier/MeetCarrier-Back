package com.kslj.mannam.domain.invitation.controller;

import com.kslj.mannam.domain.invitation.dto.InvitationRequestDto;
import com.kslj.mannam.domain.invitation.dto.InvitationResponseDto;
import com.kslj.mannam.domain.invitation.dto.RespondToInvitationDto;
import com.kslj.mannam.domain.invitation.service.InvitationService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/invitation")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    // 만남 초대장 전송
    @PostMapping
    public ResponseEntity<?> createInvitation(
            @RequestBody InvitationRequestDto dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
            long invitationId = invitationService.createInvitation(dto, userDetails.getUser());
            return ResponseEntity.ok("초대장이 전송되었습니다." + invitationId);
    }

    // 만남 초대장 조회
    @GetMapping("/{matchId}")
    public ResponseEntity<?> getInvitation(
            @PathVariable("matchId") Long matchId
    ) {
        InvitationResponseDto dto = invitationService.getInvitation(matchId);

        return ResponseEntity.ok(dto);
    }

    // 만남 초대장 수락 혹은 거절
    @PatchMapping("/respond")
    public ResponseEntity<?> acceptInvitation(
            @RequestBody RespondToInvitationDto dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        invitationService.respondToInvitation(dto, userDetails.getUser());

        return ResponseEntity.ok(dto.isAccepted() ? "수락 완료" : "거절 완료");
    }

}
