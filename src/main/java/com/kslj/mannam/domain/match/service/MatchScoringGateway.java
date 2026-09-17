package com.kslj.mannam.domain.match.service;

import com.kslj.mannam.domain.match.dto.MatchFilterRequestDto;
import com.kslj.mannam.domain.match.dto.MatchFilterResponseDto;

public interface MatchScoringGateway {
    MatchFilterResponseDto score(MatchFilterRequestDto request);
}
