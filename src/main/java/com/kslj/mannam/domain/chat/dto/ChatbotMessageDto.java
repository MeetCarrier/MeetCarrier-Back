package com.kslj.mannam.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatbotMessageDto {
    long roomId;
    long userId;
    String message;
    Boolean isVisible;
}
