package com.kslj.mannam.meeting;

import com.kslj.mannam.TestUtils;
import com.kslj.mannam.domain.match.dto.MatchRequestDto;
import com.kslj.mannam.domain.meeting.dto.MeetingRequestDto;
import com.kslj.mannam.domain.meeting.dto.MeetingResponseDto;
import com.kslj.mannam.domain.meeting.service.MeetingService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.match.service.MatchService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@Transactional
public class MeetingServiceTest {

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private TestUtils testUtils;

    private MeetingRequestDto createMeetingRequestDto(String location, String note) {
        return MeetingRequestDto.builder()
                .date(LocalDateTime.now().plusDays(1))
                .location(location)
                .note(note)
                .build();
    }

    // 대면 약속 생성 및 조회 테스트
    @Test
    public void createMeetingTest() {
        // given
        User user1 = testUtils.createAndGetTestUser();
        User user2 = testUtils.createAndGetTestUser();
        long matchId = matchService.createMatch(MatchRequestDto.builder()
                .score(90)
                .user1(user1)
                .user2(user2)
                .build());
        MeetingRequestDto requestDto = createMeetingRequestDto("강남역 카페", "서로 이야기 나눠보는 시간");

        // when
        long meetingId = meetingService.createMeeting(matchId, requestDto);
        List<MeetingResponseDto> meetings = meetingService.getMeetings(user1);

        // then
        Assertions.assertEquals(1, meetings.size());
        Assertions.assertEquals("강남역 카페", meetings.get(0).getLocation());
        Assertions.assertEquals("서로 이야기 나눠보는 시간", meetings.get(0).getNote());
    }

    // 대면 약속 수정 테스트
    @Test
    public void updateMeetingTest() {
        // given
        User user1 = testUtils.createAndGetTestUser();
        User user2 = testUtils.createAndGetTestUser();
        long matchId = matchService.createMatch(MatchRequestDto.builder()
                .score(90)
                .user1(user1)
                .user2(user2)
                .build());
        MeetingRequestDto requestDto = createMeetingRequestDto("홍대 카페", "첫 만남");

        long meetingId = meetingService.createMeeting(matchId, requestDto);

        // when
        MeetingRequestDto updatedDto = createMeetingRequestDto("신촌 카페", "장소 변경");
        meetingService.updateMeeting(meetingId, updatedDto);
        List<MeetingResponseDto> meetings = meetingService.getMeetings(user1);

        // then
        Assertions.assertEquals(1, meetings.size());
        Assertions.assertEquals("신촌 카페", meetings.get(0).getLocation());
        Assertions.assertEquals("장소 변경", meetings.get(0).getNote());
    }

    // 대면 약속 삭제 테스트
    @Test
    public void deleteMeetingTest() {
        // given
        User user1 = testUtils.createAndGetTestUser();
        User user2 = testUtils.createAndGetTestUser();
        long matchId = matchService.createMatch(MatchRequestDto.builder()
                .score(90)
                .user1(user1)
                .user2(user2)
                .build());
        MeetingRequestDto requestDto = createMeetingRequestDto("서울역 카페", "짧은 만남");

        long meetingId = meetingService.createMeeting(matchId, requestDto);

        // when
        meetingService.deleteMeeting(meetingId);

        // then
        List<MeetingResponseDto> meetings = meetingService.getMeetings(user1);
        Assertions.assertEquals(0, meetings.size());
    }
}
