package com.kslj.mannam.domain.report.dto;

import com.kslj.mannam.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplyResponseDto {
    private String replyContent;
    private LocalDateTime repliedAt;
    private String replierNickname;
}
