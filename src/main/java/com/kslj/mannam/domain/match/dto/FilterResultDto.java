package com.kslj.mannam.domain.match.dto;

import lombok.Data;

@Data
// 필터링을 거치고 나온 각 유저에 대한 매칭 점수
public class FilterResultDto {
    private long userId;
    private double finalScore;
}
