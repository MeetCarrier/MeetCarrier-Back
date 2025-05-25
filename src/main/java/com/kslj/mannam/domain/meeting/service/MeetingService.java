package com.kslj.mannam.domain.meeting.service;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.service.MatchService;
import com.kslj.mannam.domain.meeting.dto.MeetingRequestDto;
import com.kslj.mannam.domain.meeting.dto.MeetingResponseDto;
import com.kslj.mannam.domain.meeting.entity.Meeting;
import com.kslj.mannam.domain.meeting.repository.MeetingRepository;
import com.kslj.mannam.domain.notification.enums.NotificationType;
import com.kslj.mannam.domain.notification.repository.NotificationRepository;
import com.kslj.mannam.domain.notification.service.NotificationService;
import com.kslj.mannam.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MeetingService {

    private static final Logger log = LoggerFactory.getLogger(MeetingService.class);
    private final MeetingRepository meetingRepository;
    private final MatchService matchService;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

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
    public void updateMeeting(long meetingId, MeetingRequestDto dto) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("약속 정보를 찾을 수 없습니다. meetingId = " + meetingId));

        if(dto.getDate() != null) meeting.updateDate(dto.getDate());
        if(dto.getLocation() != null) meeting.updateLocation(dto.getLocation());
        if(dto.getNote() != null) meeting.updateNote(dto.getNote());
    }

    // 대면 약속 삭제
    public void deleteMeeting(long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("약속 정보를 찾을 수 없습니다. meetingId = " + meetingId));

        meetingRepository.deleteById(meetingId);
    }

    // 대면 약속 24시간 전 알람 전송
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void notifyMeeting() {
        log.info("Meeting Check Started");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next24hr = now.plusHours(24);

        List<Meeting> meetings = meetingRepository.findByDateBetween(now, next24hr);

        for (Meeting meeting : meetings) {
            User user1 = meeting.getMatch().getUser1();
            User user2 = meeting.getMatch().getUser2();

            boolean alreadyNotified1 = notificationRepository.existsByUserAndReferenceId(user1, meeting.getId());
            boolean alreadyNotified2 = notificationRepository.existsByUserAndReferenceId(user2, meeting.getId());

            if (!alreadyNotified1) {
                notificationService.createNotification(NotificationType.Meeting, user1, meeting.getId());
            }

            if (!alreadyNotified2) {
                notificationService.createNotification(NotificationType.Meeting, user2, meeting.getId());
            }
        }
    }
}
