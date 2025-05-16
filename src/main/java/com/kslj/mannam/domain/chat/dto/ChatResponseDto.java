package com.kslj.mannam.domain.chat.dto;

import com.kslj.mannam.domain.chat.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponseDto {
    private MessageType type;
    private String message;
    private String imageUrl;
    private long sender;

    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();
}
