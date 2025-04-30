package com.kslj.mannam.domain.match.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
// 파이썬에서 필터링을 거치기 위해 필요한 데이터
public class MatchFilterRequestDto {
    UUID requestId;
    List<MatchQueueRequestDto> waitingUsers;
    MatchQueueRequestDto newUser;
}
