package com.kslj.mannam.domain.user.enums;

import java.util.Arrays;

public enum SocialType {
    Kakao,
    Google;

    public static SocialType from(String value) {
        return Arrays.stream(SocialType.values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 소셜 타입: " + value));
    }
}
