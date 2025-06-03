package com.kslj.mannam.domain.invitation.dto;

import com.kslj.mannam.domain.invitation.entity.Invitation;
import com.kslj.mannam.domain.invitation.enums.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponseDto {
    private Long id;
    private String message;
    private Long senderId;
    private Long receiverId;
    private InvitationStatus status;

    public static InvitationResponseDto fromEntity(Invitation invitation) {

        return InvitationResponseDto.builder()
                .id(invitation.getId())
                .message(invitation.getMessage())
                .senderId(invitation.getSender().getId())
                .receiverId(invitation.getReceiver().getId())
                .status(invitation.getStatus())
                .build();
    }
}
