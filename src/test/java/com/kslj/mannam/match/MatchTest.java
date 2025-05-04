package com.kslj.mannam.match;

import com.kslj.mannam.TestUtils;
import com.kslj.mannam.domain.match.dto.MatchRequestDto;
import com.kslj.mannam.domain.match.dto.MatchResponseDto;
import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.enums.MatchStatus;
import com.kslj.mannam.domain.match.service.MatchService;
import com.kslj.mannam.domain.user.entity.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
public class MatchTest {

    @Autowired
    MatchService matchService;

    @Autowired
    TestUtils testUtils;

    // 매칭 정보 생성 및 조회 테스트
    @Test
    public void createMatchAndGetTest() {
        // given
        User testUser1 = testUtils.createAndGetTestUser();
        User testUser2 = testUtils.createAndGetTestUser();

        MatchRequestDto requestDto = MatchRequestDto.builder().score(70).user1Id(testUser1.getId()).user2Id(testUser2.getId()).build();

        // when
        long matchId = matchService.createMatch(requestDto);
        List<MatchResponseDto> match = matchService.getMatches(testUser1);

        // then
        Assertions.assertThat(match.get(0).getUser1Id()).isEqualTo(testUser1.getId());
        Assertions.assertThat(match.get(0).getUser2Id()).isEqualTo(testUser2.getId());
    }

    // 매칭 정보 상태 변경 테스트
    @Test
    public void updateMatchStatusTest() {
        // given
        User testUser1 = testUtils.createAndGetTestUser();
        User testUser2 = testUtils.createAndGetTestUser();

        MatchRequestDto requestDto = MatchRequestDto.builder().score(70).user1Id(testUser1.getId()).user2Id(testUser2.getId()).build();
        long matchId = matchService.createMatch(requestDto);

        // when
        matchService.updateMatchStatus(matchId, MatchStatus.Surveying);
        List<MatchResponseDto> match = matchService.getMatches(testUser1);

        // then
        Assertions.assertThat(match.get(0).getStatus()).isEqualTo(MatchStatus.Surveying);
    }

    // 매칭 정보 삭제 테스트
    @Test
    public void deleteMatchTest() {
        // given
        long matchId = 0;
        User testUser1 = testUtils.createAndGetTestUser();
        User testUser2 = testUtils.createAndGetTestUser();

        for (int i=0; i<3; i++) {
            MatchRequestDto requestDto = MatchRequestDto.builder().score(70).user1Id(testUser1.getId()).user2Id(testUser2.getId()).build();
            matchId = matchService.createMatch(requestDto);
        }

        // when
        matchService.deleteMatch(matchId);

        // then
        System.out.println("matches = " + matchService.getMatches(testUser1));
        Assertions.assertThat(matchService.getMatches(testUser1).size()).isEqualTo(2);
    }
}
