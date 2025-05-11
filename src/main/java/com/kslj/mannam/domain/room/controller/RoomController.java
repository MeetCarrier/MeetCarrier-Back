package com.kslj.mannam.domain.room.controller;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.service.MatchService;
import com.kslj.mannam.domain.room.service.RoomService;
import com.kslj.mannam.domain.survey.repository.SurveySessionRepository;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/room")
public class RoomController {
    private final MatchService matchService;
    private final RoomService roomService;
    private final SurveySessionRepository surveySessionRepository;
    private final UserService userService;

    @PostMapping("/{matchId}/{userId}/enter")
    public ResponseEntity<?> enterChatRoom(
            @PathVariable("matchId") Long matchId,
            @PathVariable("userId") Long userId
    ) {
        User currentUser = userService.getUserById(userId);

        // 1. 유저 입장 여부 저장
        matchService.markUserEnteredChat(matchId, currentUser);

        // 2. 관련 정보 조회
        Long sessionId = surveySessionRepository.findSurveySessionByMatchId(matchId).getId();
        long roomId = roomService.getRoomId(matchId);

        return ResponseEntity.ok("sessionId: " + sessionId + ", roomId: " + roomId + ", userId: " + userId + "입장");
    }
}
