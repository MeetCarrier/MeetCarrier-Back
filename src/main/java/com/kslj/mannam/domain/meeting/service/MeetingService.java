package com.kslj.mannam.domain.meeting.service;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.service.MatchService;
import com.kslj.mannam.domain.meeting.dto.MeetingRequestDto;
import com.kslj.mannam.domain.meeting.dto.MeetingResponseDto;
import com.kslj.mannam.domain.meeting.entity.Meeting;
import com.kslj.mannam.domain.meeting.repository.MeetingRepository;
import com.kslj.mannam.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MatchService matchService;

    // 새로운 대면 약속 저장
    public long createMeeting(long matchId, MeetingRequestDto requestDto) {
        Match match = matchService.getMatch(matchId);

        boolean exists = meetingRepository.existsByMatchId(matchId);
        if (exists) {
            throw new IllegalStateException("이미 약속이 존재합니다.");
        }

        Meeting newMeeting = Meeting.builder()
                .date(requestDto.getDate())
                .location(requestDto.getLocation())
                .note(requestDto.getNote())
                .match(match)
                .build();

        Meeting savedMeeting = meetingRepository.save(newMeeting);

        return savedMeeting.getId();
    }

    // 대면 약속 조회
    public List<MeetingResponseDto> getMeetings(User user) {
        List<Meeting> meetings = meetingRepository.findAllByUserId(user.getId());

        return meetings.stream()
                .map(MeetingResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 대면 약속 수정
    public void updateMeeting(long meetingId, MeetingRequestDto requestDto) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("약속 정보를 찾을 수 없습니다. meetingId = " + meetingId));

        meeting.updateMeeting(requestDto.getDate(), requestDto.getLocation(), requestDto.getNote());
    }

    // 대면 약속 삭제
    public void deleteMeeting(long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("약속 정보를 찾을 수 없습니다. meetingId = " + meetingId));

        meetingRepository.deleteById(meetingId);
    }
}
