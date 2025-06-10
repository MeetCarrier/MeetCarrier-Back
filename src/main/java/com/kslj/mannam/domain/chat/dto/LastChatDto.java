package com.kslj.mannam.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LastChatDto {
    private Long roomId;
    private String lastMessage;
    private Integer unreadCount;
    private LocalDateTime lastMessageAt;
}
