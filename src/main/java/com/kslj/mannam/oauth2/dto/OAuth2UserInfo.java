package com.kslj.mannam.oauth2.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OAuth2UserInfo {
    private final String socialId;
    private final String provider;
}
