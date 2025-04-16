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
    MessageType type;
    String message;
    String imageUrl;

    public ChatResponseDto toChatResponseDto(User sender) {
        return ChatResponseDto.builder()
                .messageType(type)
                .message(message)
                .imageUrl(imageUrl)
                .sender(sender.getId())
                .build();
    }
}
