package com.kslj.mannam.domain.match.service;

import com.kslj.mannam.domain.match.dto.MatchRequestDto;
import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.enums.MatchStatus;
import com.kslj.mannam.domain.match.repository.MatchRepository;
import com.kslj.mannam.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MatchService {

    private final MatchRepository matchRepository;

    // 매칭 정보 생성
    @Transactional
    public long createMatch(MatchRequestDto matchRequestDto) {
        Match newMatch = Match.builder()
                .score(matchRequestDto.getScore())
                .user1(matchRequestDto.getUser1())
                .user2(matchRequestDto.getUser2())
                .build();

        Match savedMatch = matchRepository.save(newMatch);

        return savedMatch.getId();
    }

    // 매칭 정보들 조회
    @Transactional
    public List<Match> getMatches(User user) {
        return matchRepository.findAllByUser1OrUser2(user, user);
    }

    // 매칭 정보 업데이트
    @Transactional
    public long updateMathStatus(long matchId, MatchStatus status) {
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
