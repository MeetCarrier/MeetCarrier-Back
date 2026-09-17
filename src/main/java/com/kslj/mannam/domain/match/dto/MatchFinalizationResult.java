package com.kslj.mannam.domain.match.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchFinalizationResult {
    private final boolean success;
    private final Long matchId;
    private final Long surveySessionId;

    public static MatchFinalizationResult conflict() {
        return new MatchFinalizationResult(false, null, null);
    }
}
