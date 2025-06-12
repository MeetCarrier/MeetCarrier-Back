package com.kslj.mannam.domain.chat.dto;

import com.kslj.mannam.domain.chat.enums.MessageType;
import com.kslj.mannam.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {
    long roomId;
    long userId;
    MessageType type;
    String message;
    String imageUrl;
    Boolean isVisible;

    public ChatResponseDto fromEntity(User sender) {
        return ChatResponseDto.builder()
                .type(type)
                .message(message)
                .imageUrl(imageUrl)
                .sender(sender.getId())
                .build();
    }
}
