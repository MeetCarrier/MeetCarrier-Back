package com.kslj.mannam.domain.invitation.dto;

import lombok.Data;

@Data
public class InvitationRequestDto {
    private Long matchId;
    private String message;
    private Long receiverId;
}
