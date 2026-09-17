package com.kslj.mannam.domain.match.dto;

import com.kslj.mannam.domain.match.enums.MatchRequestState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchRequestStateDto {
    private UUID requestId;
    private long userId;
    private MatchRequestState status;
    private LocalDateTime requestedAt;
    private boolean duplicate;
    private String message;
}
