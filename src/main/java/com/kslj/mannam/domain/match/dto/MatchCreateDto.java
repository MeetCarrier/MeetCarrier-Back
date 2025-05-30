package com.kslj.mannam.domain.match.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
// 매칭 생성 시 저장할 데이터
public class MatchCreateDto {
    double score;
    long user1Id;
    long user2Id;
}
