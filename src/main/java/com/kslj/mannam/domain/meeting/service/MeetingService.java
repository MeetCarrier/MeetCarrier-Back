package com.kslj.mannam.domain.meeting.service;

import com.kslj.mannam.domain.chat.service.ChatService;
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
import com.kslj.mannam.domain.room.entity.Room;
import com.kslj.mannam.domain.room.repository.RoomRepository;
import com.kslj.mannam.domain.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
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
    private final RoomRepository roomRepository;
    private final ChatService chatService;

    // 새로운 대면 약속 저장
    @Transactional
    public long createMeeting(long matchId, User sender, MeetingRequestDto requestDto) {
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
                .senderId(sender.getId())
                .status(MeetingStatus.ACCEPTED)
                .build();

        Meeting savedMeeting = meetingRepository.save(newMeeting);

        chatService.saveChatMessageWithoutNotification(matchId, sender, "만남 약속 일정을 전송했습니다. 확인해주세요!");

        return savedMeeting.getId();
    }

    // 대면 약속 목록 전체 조회
    @Transactional(readOnly = true)
    public List<MeetingResponseDto> getMeetings(User user) {
        List<Meeting> meetings = meetingRepository.findAllByUserId(user.getId());

        return meetings.stream()
                .map(meeting -> MeetingResponseDto.fromEntity(meeting, user))
                .collect(Collectors.toList());
    }

    // matchId로 대면 약속 일정 조회
    @Transactional(readOnly = true)
    public MeetingResponseDto getMeeting(long matchId, User user) {
        Match match = matchService.getMatch(matchId);
        Meeting meeting = meetingRepository.findByMatch(match);

        return MeetingResponseDto.fromEntity(meeting, user);
    }

    // 대면 약속 수정
    @Transactional
    public void updateMeeting(long meetingId, MeetingRequestDto dto, User user) throws AccessDeniedException {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("약속 정보를 찾을 수 없습니다. meetingId = " + meetingId));

        validateUserAuthorityForMeeting(user, meeting);

        if (meeting.getStatus() != MeetingStatus.ACCEPTED) {
            throw new IllegalStateException("확정된 약속만 수정할 수 있습니다.");
        }

        if(dto.getDate() != null) {
            meeting.updateDate(dto.getDate());

            Match match = meeting.getMatch();

            // 채팅방 종료 시간 갱신
            Room room = roomRepository.getRoomByMatchId(meeting.getMatch().getId());
            room.updateDeactivationTime(meeting.getDate().plusHours(24));

            chatService.saveChatMessageWithoutNotification(match.getId(), user, "만남 일정이 변경되었어요! 확인해보세요!");
        }
        if(dto.getLocation() != null) {
            meeting.updateLocation(dto.getLocation());
        }
        if(dto.getNote() != null) meeting.updateNote(dto.getNote());
    }

    // 대면 약속 삭제
    @Transactional
    public void deleteMeeting(long meetingId, User user) throws AccessDeniedException {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("약속 정보를 찾을 수 없습니다. meetingId = " + meetingId));

        validateUserAuthorityForMeeting(user, meeting);

        meetingRepository.deleteById(meetingId);
    }

    // 대면 약속 신청에 동의
    @Transactional
    public void confirmMeeting(User currentUser, long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("약속 정보를 찾을 수 없습니다. meetingId = " + meetingId));

        if (meeting.getStatus() != MeetingStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 만남 일정입니다.");
        }

        if (meeting.getSenderId().equals(currentUser.getId())) {
            throw new IllegalStateException("상대방만 수락 혹은 거절할 수 있습니다.");
        }

        Match match = meeting.getMatch();

        meeting.confirm();

        // 채팅방 종료 시간 갱신
        Room room = roomRepository.getRoomByMatchId(match.getId());
        room.updateDeactivationTime(meeting.getDate().plusHours(24));

        // 알림 전송
        notificationService.createNotification(NotificationType.MeetingAccepted, match.getUser1(), room.getId());
        notificationService.createNotification(NotificationType.MeetingAccepted, match.getUser2(), room.getId());

        // 채팅방에 알림
        chatService.saveChatMessageWithoutNotification(match.getId(), currentUser, "만남 일정이 확정되었어요!");
    }

    // 대면 약속 신청에 거절
    @Transactional
    public void rejectMeeting(User currentUser, long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("약속 정보를 찾을 수 없습니다. meetingId = " + meetingId));

        if (meeting.getStatus() != MeetingStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 만남 일정입니다.");
        }

        if (meeting.getSenderId().equals(currentUser.getId())) {
            throw new IllegalStateException("상대방만 수락 혹은 거절할 수 있습니다.");
        }

        Match match = meeting.getMatch();

        meeting.reject();

        // 알림 전송
        Room room = roomRepository.getRoomByMatchId(match.getId());
        User receiver = getOtherUser(currentUser, meeting);
        notificationService.createNotification(NotificationType.MeetingRejected, receiver, room.getId());

        // 채팅방에 알림
        chatService.saveChatMessageWithoutNotification(match.getId(), currentUser, "만남 일정이 거절되었어요... 다시 한 번 일정을 조율해보세요!");
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
            if (meeting.getStatus() != MeetingStatus.ACCEPTED) {
                continue;
            }

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
            throw new IllegalStateException("이 약속과 관련없는 사용자입니다.");
        }
    }

    // 권한 검사를 위한 private 메서드
    private void validateUserAuthorityForMeeting(User user, Meeting meeting) throws AccessDeniedException {
        User user1 = meeting.getMatch().getUser1();
        User user2 = meeting.getMatch().getUser2();

        if (!user.getId().equals(user1.getId()) && !user.getId().equals(user2.getId())) {
            throw new AccessDeniedException("이 약속을 수정하거나 삭제할 권한이 없습니다."); // 혹은 다른 적절한 예외
        }
    }
}
