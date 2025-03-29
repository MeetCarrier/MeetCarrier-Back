package com.kslj.mannam.oauth2.util;

import com.kslj.mannam.oauth2.dto.OAuth2UserInfo;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class OAuth2UserInfoExtractor {
    public static OAuth2UserInfo extract(OAuth2User oAuth2User, String registrationId) {

        String oauthId = null;

        // 공급자별로 데이터 처리
        if (registrationId.equalsIgnoreCase("google")) {
            // Google은 고유 Id가 'sub'필드에 저장되어 있음
            oauthId = oAuth2User.getAttribute("sub");
        }
        else if (registrationId.equalsIgnoreCase("kakao")) {
            oauthId = Long.toString(oAuth2User.getAttribute("id"));
        }

        return new OAuth2UserInfo(oauthId, registrationId);
    }
}
