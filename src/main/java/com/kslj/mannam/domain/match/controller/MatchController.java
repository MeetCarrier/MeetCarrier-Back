package com.kslj.mannam.domain.match.controller;

import com.kslj.mannam.domain.match.dto.MatchRequestDto;
import com.kslj.mannam.domain.match.dto.MatchResponseDto;
import com.kslj.mannam.domain.match.dto.StatusUpdateDto;
import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.enums.MatchStatus;
import com.kslj.mannam.domain.match.service.MatchService;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/matches")
public class MatchController {

    private final MatchService matchService;
    private final UserService userService;

    // 현재 유저 매칭 목록 조회
    @GetMapping
    public ResponseEntity<?> getMatches(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<MatchResponseDto> matches = matchService.getMatches(userService.getUserById(1));

        return ResponseEntity.ok(matches);
    }

    // 매칭 상태 업데이트
    @PatchMapping("/{matchId}")
    public ResponseEntity<?> updateStatus(@PathVariable("matchId") long matchId,
                                          @RequestBody StatusUpdateDto dto) {
        MatchStatus matchStatus = dto.getMatchStatus();

        matchService.updateMathStatus(matchId, matchStatus);
        return ResponseEntity.ok("상태가 업데이트되었습니다. matchId = " + matchId);
    }

    // 매칭 삭제
    @DeleteMapping("/{matchId}")
    public ResponseEntity<?> deleteMatch(@PathVariable("matchId") long matchId) {
        long deletedMatchId = matchService.deleteMatch(matchId);

        return ResponseEntity.ok("매칭 정보가 삭제되었습니다. deletedMatchId = " + deletedMatchId);
    }
}
