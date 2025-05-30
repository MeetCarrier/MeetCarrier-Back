package com.kslj.mannam.domain.match.controller;

import com.kslj.mannam.domain.match.dto.MatchRequestResponseDto;
import com.kslj.mannam.domain.match.service.MatchRequestService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
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
public class MatchRequestController {

    private final MatchRequestService matchRequestService;

    // 매칭 요청
    @PostMapping("/request")
    public ResponseEntity<?> sendMatchRequest(@RequestParam("receiverId") long receiverId,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        long senderId = userDetails.getUser().getId();
        matchRequestService.createMatchRequest(senderId, receiverId);
        return ResponseEntity.ok("매칭 요청을 전송했습니다.");
    }

    // 매칭 수락
    @PostMapping("/respond")
    public ResponseEntity<?> respondToMatchRequest(@RequestBody MatchRequestResponseDto dto,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        boolean matched = matchRequestService.processRespond(userDetails.getId(), dto.getRequestId(),dto.isAccepted());

        if (matched) {
            return ResponseEntity.ok("매칭 수락");
        } else {
            return ResponseEntity.ok("매칭 거절");
        }
    }
}