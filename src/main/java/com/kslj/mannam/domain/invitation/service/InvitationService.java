package com.kslj.mannam.domain.invitation.service;

import com.kslj.mannam.domain.chat.service.ChatService;
import com.kslj.mannam.domain.invitation.dto.InvitationRequestDto;
import com.kslj.mannam.domain.invitation.dto.InvitationResponseDto;
import com.kslj.mannam.domain.invitation.dto.RespondToInvitationDto;
import com.kslj.mannam.domain.invitation.entity.Invitation;
import com.kslj.mannam.domain.invitation.enums.InvitationStatus;
import com.kslj.mannam.domain.invitation.repository.InvitationRepository;
import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.enums.MatchStatus;
import com.kslj.mannam.domain.match.service.MatchService;
import com.kslj.mannam.domain.notification.enums.NotificationType;
import com.kslj.mannam.domain.notification.service.NotificationService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final MatchService matchService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final ChatService chatService;

    // 만남 초대장 생성
    @Transactional
    public long createInvitation(InvitationRequestDto dto, User sender) {
        Match match = matchService.getMatch(dto.getMatchId());

        Invitation existing = invitationRepository.findByMatch(match);
        if (existing != null) {
            if (existing.getStatus() == InvitationStatus.PENDING) {
                throw new IllegalStateException("상대방이 확인하지 않은 초대장이 이미 존재합니다.");
            } else {
                invitationRepository.delete(existing);
                invitationRepository.flush();
            }
        }

        User receiver = userService.getUserById(dto.getReceiverId());

        Invitation invitation = Invitation.builder()
                .message(dto.getMessage())
                .sender(sender)
                .receiver(receiver)
                .match(match)
                .build();

        Invitation saved = invitationRepository.save(invitation);

        notificationService.createNotification(NotificationType.InvitationRequest, receiver, null);

        chatService.saveChatMessageWithoutNotification(match.getId(), sender, "만남초대장이 전송되었습니다. + 버튼을 눌러 확인해보세요!");

        return saved.getId();
    }

    // 만남 초대장 조회
    @Transactional(readOnly = true)
    public InvitationResponseDto getInvitation(Long matchId) {
        Match match = matchService.getMatch(matchId);

        Invitation invitation = invitationRepository.findByMatch(match);

        if (invitation == null) {
            throw new EntityNotFoundException("해당 초대장을 찾을 수 없습니다. Match ID: " + matchId);
        }

        return InvitationResponseDto.fromEntity(invitation);
    }

    // 만남 초대장 수락 혹은 거절
    @Transactional
    public void respondToInvitation(RespondToInvitationDto dto, User receiver) {
        Match match = matchService.getMatch(dto.getMatchId());

        Invitation invitation = invitationRepository.findByMatch(match);
        if (invitation == null) {
            throw new EntityNotFoundException("초대장 정보가 없습니다. matchId: " + dto.getMatchId());
        }

        if (!invitation.getReceiver().equals(receiver))
        {
            throw new IllegalStateException("수신자가 잘못 지정되었습니다.");
        }
        else if (!invitation.getStatus().equals(InvitationStatus.PENDING))
        {
            throw new IllegalStateException("이미 처리된 초대장입니다.");
        }

        User sender = invitation.getSender();
        if (dto.isAccepted()) {
            invitation.accept();
            matchService.updateMatchStatus(dto.getMatchId(), MatchStatus.Meeting);
            notificationService.createNotification(NotificationType.InvitationAccepted, sender, null);
        } else {
            invitation.reject();
            notificationService.createNotification(NotificationType.InvitationRejected, sender, null);
        }
    }
}
