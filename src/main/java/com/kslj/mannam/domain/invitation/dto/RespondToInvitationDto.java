package com.kslj.mannam.domain.invitation.dto;

import lombok.Data;

@Data
public class RespondToInvitationDto {
    private Long matchId;
    private Long senderId;
    private boolean accepted;
}
