package com.kslj.mannam.domain.match.service;

import com.kslj.mannam.domain.chat.dto.LastChatDto;
import com.kslj.mannam.domain.chat.service.ChatService;
import com.kslj.mannam.domain.match.dto.MatchCreateDto;
import com.kslj.mannam.domain.match.dto.MatchResponseDto;
import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.enums.MatchStatus;
import com.kslj.mannam.domain.match.repository.MatchRepository;
import com.kslj.mannam.domain.meeting.entity.Meeting;
import com.kslj.mannam.domain.meeting.enums.MeetingStatus;
import com.kslj.mannam.domain.meeting.repository.MeetingRepository;
import com.kslj.mannam.domain.room.entity.Room;
import com.kslj.mannam.domain.room.enums.RoomStatus;
import com.kslj.mannam.domain.room.repository.RoomRepository;
import com.kslj.mannam.domain.room.service.RoomService;
import com.kslj.mannam.domain.survey.repository.SurveySessionRepository;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.enums.ActionType;
import com.kslj.mannam.domain.user.service.UserActionLogService;
import com.kslj.mannam.domain.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final SurveySessionRepository surveySessionRepository;
    private final UserService userService;
    private final RoomService roomService;
    private final UserActionLogService userActionLogService;
    private final ChatService chatService;

    private final RoomRepository roomRepository;
    private final MeetingRepository meetingRepository;

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
            LastChatDto lastChatDto = null;

            if (status != MatchStatus.Surveying && status != MatchStatus.Survey_Cancelled && status != MatchStatus.Matched) {
                Room room = roomRepository.findRoomByMatchId(match.getId())
                        .orElse(null);

                if (room != null) {
                    roomId = room.getId();
                    lastChatDto = chatService.getLastChatInfo(roomId, user.getId());
                }
            }

            responses.add(MatchResponseDto.fromEntity(match, sessionId, roomId, user, lastChatDto));
        }

        return responses;
    }

    // 특정 매칭 정보 조회
    @Transactional(readOnly = true)
    public Match getMatch(Long matchId) {
        return matchRepository.findById(matchId).orElseThrow(
                () -> new EntityNotFoundException("매칭 정보가 없습니다. matchId=" + matchId));
    }

    // 유저로 매칭 정보 검색
    @Transactional(readOnly = true)
    public Match findExistingMatch(Long userAId, Long userBId) {
        User userA = userService.getUserById(userAId);
        User userB = userService.getUserById(userBId);

        return matchRepository.findMatchByUsers(userA, userB).orElseThrow(
                () -> new EntityNotFoundException("매칭 정보가 없습니다. userA=" + userA + " userB=" + userB)
        );
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
            throw new IllegalStateException("User not part of this match");
        }

        match.markUserEntered(user);
    }

    // 유저 기준으로 roomId 조회
    @Transactional(readOnly = true)
    public List<Long> getRoomByUserId(Long userId) {
        User currentUser = userService.getUserById(userId);
        List<Long> roomIds = new ArrayList<>();
        List<MatchResponseDto> matches = getMatches(currentUser);
        matches.forEach(match -> {
            if (match.getStatus() == MatchStatus.Chatting)
                roomIds.add(match.getRoomId());
        });

        return roomIds;
    }

    // 매칭 가능 여부 조회 메서드
    public boolean canRequestNewMatch(User user) {
        // 진행 중으로 간주할 매칭 상태
        Set<MatchStatus> activeStatus = Set.of(
                MatchStatus.Surveying,
                MatchStatus.Chatting,
                MatchStatus.Meeting
        );

        // 매칭 기록 조회
        List<Match> userMatches = matchRepository.findAllByUser1OrUser2(user, user);

        // 매칭 상태 확인
        boolean hasActiveMatch = userMatches.stream()
                .anyMatch(match -> activeStatus.contains(match.getStatus()));

        return !hasActiveMatch;
    }

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkRoomTimeAndDeactivate() {
        log.info("Room Time Check and Match Status Update Started");
        List<Room> rooms = roomRepository.findAllByStatusAndDeactivationTimeBefore(RoomStatus.Activate, LocalDateTime.now());

        for (Room room : rooms) {
            Match match = room.getMatch();

            if (match.getStatus() == MatchStatus.Chatting) {
                updateMatchStatus(match.getId(), MatchStatus.Chat_Cancelled);
            } else if (match.getStatus() == MatchStatus.Meeting) {
                // Null-safe하게 미팅 정보 조회 및 상태 확인
                Meeting meeting = meetingRepository.findByMatch(match);
                if (meeting.getStatus() == MeetingStatus.ACCEPTED) {
                    updateMatchStatus(match.getId(), MatchStatus.Reviewing);
                } else {
                    updateMatchStatus(match.getId(), MatchStatus.Chat_Cancelled);
                }
            } else {
                // 예기치 않은 상태에 대한 처리 (예: 로깅)
                log.warn("Room deactivation schedule: Unexpected match status '{}' for matchId {}", match.getStatus(), match.getId());
                // 필요하다면 기본 상태로 변경
                updateMatchStatus(match.getId(), MatchStatus.Reviewing);
            }

            room.updateStatus(RoomStatus.Deactivate);
            // roomRepository.save(room); // 더티 체킹으로 자동 반영
        }
    }
}
