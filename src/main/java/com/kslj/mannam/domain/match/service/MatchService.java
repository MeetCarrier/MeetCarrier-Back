package com.kslj.mannam.domain.match.service;

import com.kslj.mannam.domain.match.dto.MatchCreateDto;
import com.kslj.mannam.domain.match.dto.MatchResponseDto;
import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.enums.MatchStatus;
import com.kslj.mannam.domain.match.repository.MatchRepository;
import com.kslj.mannam.domain.room.service.RoomService;
import com.kslj.mannam.domain.survey.repository.SurveySessionRepository;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.enums.ActionType;
import com.kslj.mannam.domain.user.service.UserActionLogService;
import com.kslj.mannam.domain.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final SurveySessionRepository surveySessionRepository;
    private final UserService userService;
    private final RoomService roomService;
    private final UserActionLogService userActionLogService;

    // 매칭 정보 생성
    @Transactional
    public long createMatch(MatchCreateDto matchCreateDto) {
        User user1 = userService.getUserById(matchCreateDto.getUser1Id());
        User user2 = userService.getUserById(matchCreateDto.getUser2Id());

        Match newMatch = Match.builder()
                .score(matchCreateDto.getScore())
                .user1(user1)
                .user2(user2)
                .build();

        Match savedMatch = matchRepository.save(newMatch);
        userActionLogService.logUserAction(user1, ActionType.COMPLETE_MATCH);
        userActionLogService.logUserAction(user2, ActionType.COMPLETE_MATCH);

        return savedMatch.getId();
    }

    // 매칭 정보들 조회
    @Transactional(readOnly = true)
    public List<MatchResponseDto> getMatches(User user) {
        List<Match> matches = matchRepository.findAllByUser1OrUser2(user, user);
        List<MatchResponseDto> responses = new ArrayList<>();

        if (matches.isEmpty()) {
            return responses;
        }

        for (Match match : matches) {
            Long sessionId = surveySessionRepository.findSurveySessionByMatchId(match.getId()).getId();

            Long roomId = null;
            MatchStatus status = match.getStatus();

            if (status != MatchStatus.Surveying && status != MatchStatus.Survey_Cancelled && status != MatchStatus.Matched) {
                roomId = roomService.getRoomId(match.getId());
            }

            responses.add(MatchResponseDto.fromEntity(match, sessionId, roomId, user));
        }

        return responses;
    }

    // 특정 매칭 정보 조회
    @Transactional(readOnly = true)
    public Match getMatch(Long matchId) {
        return matchRepository.findById(matchId).orElseThrow(
                () -> new RuntimeException("매칭 정보가 없습니다. matchId=" + matchId));
    }

    // 매칭 정보 업데이트
    @Transactional
    public void updateMatchStatus(long matchId, MatchStatus status) {
        Optional<Match> match = matchRepository.findById(matchId);

        if (match.isEmpty()) {
            throw new EntityNotFoundException("매칭 정보가 없습니다. matchId=" + matchId);
        } else {
            Match targetMatch = match.get();

            targetMatch.updateStatus(status);
            matchRepository.save(targetMatch);
            if(status == MatchStatus.Completed){
                userActionLogService.logUserAction(match.get().getUser1(), ActionType.COMPLETE_MATCH);
                userActionLogService.logUserAction(match.get().getUser2(), ActionType.COMPLETE_MATCH);
            }
        }
    }

    // 매칭 정보 삭제
    @Transactional
    public long deleteMatch(long matchId) {
        Optional<Match> match = matchRepository.findById(matchId);

        if (match.isEmpty()) {
            throw new EntityNotFoundException("매칭 정보가 없습니다. matchId=" + matchId);
        } else {
            Match targetMatch = match.get();

            matchRepository.delete(targetMatch);
            return matchId;
        }
    }

    // 채팅방으로 넘어갔는지 여부 저장
    @Transactional
    public void markUserEnteredChat(Long matchId, User user) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Match not found"));

        if (!match.hasUser(user)) {
            throw new IllegalArgumentException("User not part of this match");
        }

        match.markUserEntered(user);
    }
}
