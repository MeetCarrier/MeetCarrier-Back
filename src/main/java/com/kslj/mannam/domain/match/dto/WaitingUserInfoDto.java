package com.kslj.mannam.domain.match.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WaitingUserInfoDto {
    MatchQueueRequestDto userData;
    Map<Long, Double> scoreMap;
}
