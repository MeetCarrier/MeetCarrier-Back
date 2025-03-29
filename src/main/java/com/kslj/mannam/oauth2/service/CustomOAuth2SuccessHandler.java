package com.kslj.mannam.oauth2.service;

import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.repository.UserRepository;
import com.kslj.mannam.oauth2.entity.CustomOAuth2User;
import com.kslj.mannam.security.SecurityContextService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final SecurityContextService securityContextService;
    private final HttpSession httpSession;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String socialId = oAuth2User.getOAuth2UserId();
        String provider = oAuth2User.getProvider();

        // 회원가입 여부 확인
        Optional<User> userOpt = userRepository.findBySocialId(socialId);

        if (userOpt.isEmpty()) {
            // 새로운 회원 -> 추가 정보 입력창으로 이동
            httpSession.setAttribute("UNREGISTERED_SOCIAL_ID", socialId);
            httpSession.setAttribute("SOCIAL_TYPE", provider);
            response.sendRedirect("/oauth/signup/detail");
        } else {
            User user = userOpt.get();
            securityContextService.refreshUserDetails(user.getSocialId());

            response.sendRedirect("/main");
        }
    }
}
