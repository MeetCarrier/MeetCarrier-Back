package com.kslj.mannam.domain.chat.dto;

import lombok.Data;

@Data
public class ChatLeaveMessageDto {
    long roomId;
    String reasonCodes;
    String customReason;
}
