package com.kslj.mannam.domain.meeting.service;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.service.MatchService;
import com.kslj.mannam.domain.meeting.dto.MeetingRequestDto;
import com.kslj.mannam.domain.meeting.dto.MeetingResponseDto;
import com.kslj.mannam.domain.meeting.entity.Meeting;
import com.kslj.mannam.domain.meeting.enums.MeetingStatus;
import com.kslj.mannam.domain.meeting.repository.MeetingRepository;
import com.kslj.mannam.domain.notification.enums.NotificationType;
import com.kslj.mannam.domain.notification.repository.NotificationRepository;
import com.kslj.mannam.domain.notification.service.NotificationService;
import com.kslj.mannam.domain.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
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
    @Transactional
    public long createMeeting(long matchId, MeetingRequestDto requestDto) {
        Match match = matchService.getMatch(matchId);

        boolean exists = meetingRepository.existsByMatchIdAndStatus(matchId, MeetingStatus.PENDING);
        if (exists) {
            throw new IllegalStateException("아직 이전 요청을 상대방이 확인하지 않았어요.");
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

    // 대면 약속 목록 전체 조회
    @Transactional(readOnly = true)
    public List<MeetingResponseDto> getMeetings(User user) {
        List<Meeting> meetings = meetingRepository.findAllByUserId(user.getId());

        return meetings.stream()
                .map(MeetingResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // matchId로 대면 약속 일정 조회
    @Transactional(readOnly = true)
    public MeetingResponseDto getMeeting(long matchId) {
        Match match = matchService.getMatch(matchId);
        Meeting meeting = meetingRepository.findByMatch(match);

        return MeetingResponseDto.fromEntity(meeting);
    }

    // 대면 약속 수정
    @Transactional
    public void updateMeeting(long meetingId, MeetingRequestDto dto) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("약속 정보를 찾을 수 없습니다. meetingId = " + meetingId));

        if (meeting.getStatus() != MeetingStatus.ACCEPTED) {
            throw new IllegalStateException("확정된 약속만 수정할 수 있습니다.");
        }

        if(dto.getDate() != null && dto.getLocation() != null) {
            meeting.updateSchedule(dto.getDate(), dto.getLocation());
        }
        if(dto.getNote() != null) meeting.updateNote(dto.getNote());
    }

    // 대면 약속 삭제
    @Transactional
    public void deleteMeeting(long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("약속 정보를 찾을 수 없습니다. meetingId = " + meetingId));

        meetingRepository.deleteById(meetingId);
    }

    // 대면 약속 신청에 동의
    @Transactional
    public void confirmMeeting(User currentUser, long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("약속 정보를 찾을 수 없습니다. meetingId = " + meetingId));

        meeting.confirm();

        // 알림 전송
        notificationService.createNotification(NotificationType.MeetingAccepted, meeting.getMatch().getUser1(), null);
        notificationService.createNotification(NotificationType.MeetingAccepted, meeting.getMatch().getUser2(), null);
    }

    // 대면 약속 신청에 거절
    @Transactional
    public void rejectMeeting(User currentUser, long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("약속 정보를 찾을 수 없습니다. meetingId = " + meetingId));

        meeting.reject();

        // 알림 전송
        User receiver = getOtherUser(currentUser, meeting);
        notificationService.createNotification(NotificationType.MeetingRejected, receiver, null);
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

    private User getOtherUser(User currentUser, Meeting meeting) {
        User user1 = meeting.getMatch().getUser1();
        User user2 = meeting.getMatch().getUser2();

        if (currentUser.getId().equals(user1.getId())) {
            return user2;
        } else if (currentUser.getId().equals(user2.getId())) {
            return user1;
        } else {
            throw new IllegalArgumentException("이 약속과 관련없는 사용자입니다.");
        }
    }
}
