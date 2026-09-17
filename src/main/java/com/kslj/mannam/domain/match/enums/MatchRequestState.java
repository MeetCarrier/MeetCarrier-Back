package com.kslj.mannam.domain.match.enums;

public enum MatchRequestState {
    QUEUED,
    PROCESSING,
    WAITING,
    RESERVED,
    MATCHED,
    CANCELLED,
    TIMED_OUT,
    FAILED;

    public boolean isActive() {
        return this == QUEUED || this == PROCESSING || this == WAITING || this == RESERVED;
    }
}
