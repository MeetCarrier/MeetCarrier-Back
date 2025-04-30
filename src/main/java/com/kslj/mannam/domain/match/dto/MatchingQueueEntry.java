package com.kslj.mannam.domain.match.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 큐에 들어온 유저 정보 + 들어온 시각 저장
public class MatchingQueueEntry {
    private MatchQueueRequestDto userData;     // 유저 정보
    private LocalDateTime joinTime;     // 큐에 들어온 시간

    @Builder.Default
    private Map<Long, Double> scoreMap = new HashMap<>();   // 비교 대상 유저 ID -> 매칭 점수
}
