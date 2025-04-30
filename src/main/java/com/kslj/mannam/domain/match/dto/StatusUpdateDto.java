package com.kslj.mannam.domain.match.dto;

import com.kslj.mannam.domain.match.enums.MatchStatus;
import lombok.Data;

@Data
// 상태 업데이트 시 받을 데이터
public class StatusUpdateDto {
    private MatchStatus matchStatus;
}
