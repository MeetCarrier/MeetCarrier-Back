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
// 파이썬에서 필터링 거친 결과
public class MatchFilterResponseDto {
    UUID requestId;
    List<FilterResultDto> filterResults;    // 큐에 있는 유저들에 대한 매칭 점수
}
