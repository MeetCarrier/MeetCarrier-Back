package com.kslj.mannam.domain.match.dto;

import com.kslj.mannam.domain.match.enums.MatchStatus;
import lombok.Data;

@Data
public class StatusUpdateDto {
    private MatchStatus matchStatus;
}
