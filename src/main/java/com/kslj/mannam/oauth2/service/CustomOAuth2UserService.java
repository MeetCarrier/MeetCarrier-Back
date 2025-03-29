package com.kslj.mannam.oauth2.service;

import com.kslj.mannam.oauth2.dto.OAuth2UserInfo;
import com.kslj.mannam.oauth2.entity.CustomOAuth2User;
import com.kslj.mannam.oauth2.util.OAuth2UserInfoExtractor;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 로그인 시도한 OAuth2 공급자 확인
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = OAuth2UserInfoExtractor.extract(oAuth2User, registrationId);
        String oauthId = userInfo.getSocialId();
        String provider = userInfo.getProvider();
        log.info("oauthId: {}, provider: {}", userInfo.getSocialId(), provider);

        return new CustomOAuth2User(oAuth2User, oauthId, provider);
    }
}
