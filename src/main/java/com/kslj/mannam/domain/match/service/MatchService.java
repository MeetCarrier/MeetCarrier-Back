package com.kslj.mannam.domain.match.service;

import com.kslj.mannam.domain.match.dto.MatchRequestDto;
import com.kslj.mannam.domain.match.dto.MatchResponseDto;
import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.enums.MatchStatus;
import com.kslj.mannam.domain.match.repository.MatchRepository;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final UserService userService;

    // 매칭 정보 생성
    @Transactional
    public long createMatch(MatchRequestDto matchRequestDto) {
        Match newMatch = Match.builder()
                .score(matchRequestDto.getScore())
                .user1(userService.getUserById(matchRequestDto.getUser1Id()))
                .user2(userService.getUserById(matchRequestDto.getUser2Id()))
                .build();

        Match savedMatch = matchRepository.save(newMatch);

        return savedMatch.getId();
    }

    // 매칭 정보들 조회
    @Transactional
    public List<MatchResponseDto> getMatches(User user) {
        List<Match> matches = matchRepository.findAllByUser1OrUser2(user, user);

        return matches.stream()
                .map(MatchResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 특정 매칭 정보 조회
    @Transactional
    public Match getMatch(Long matchId) {
        return matchRepository.findById(matchId).orElseThrow(
                () -> new RuntimeException("매칭 정보가 없습니다. matchId=" + matchId));
    }

    // 매칭 정보 업데이트
    @Transactional
    public long updateMatchStatus(long matchId, MatchStatus status) {
        Optional<Match> match = matchRepository.findById(matchId);

        if (match.isEmpty()) {
            throw new RuntimeException("매칭 정보가 없습니다. matchId=" + matchId);
        } else {
            Match targetMatch = match.get();

            targetMatch.updateStatus(status);
            return matchRepository.save(targetMatch).getId();
        }
    }

    // 매칭 정보 삭제
    @Transactional
    public long deleteMatch(long matchId) {
        Optional<Match> match = matchRepository.findById(matchId);

        if (match.isEmpty()) {
            throw new RuntimeException("매칭 정보가 없습니다. matchId=" + matchId);
        } else {
            Match targetMatch = match.get();

            matchRepository.delete(targetMatch);
            return matchId;
        }
    }
}
