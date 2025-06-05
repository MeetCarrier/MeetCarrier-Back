package com.kslj.mannam.oauth2.controller;

import com.kslj.mannam.domain.user.dto.UserSignUpRequestDto;
import com.kslj.mannam.domain.user.enums.SocialType;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.oauth2.dto.OAuth2RegistrationDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/api/oauth/signup")
@RequiredArgsConstructor
public class OAuthController {

    private final UserService userService;

    @GetMapping("/detail")
    public String showSignUpForm(HttpSession session) {
        if (session.getAttribute("UNREGISTERED_SOCIAL_ID") == null || session.getAttribute("SOCIAL_TYPE") == null) {
            return "redirect:/Login"; // 로그인 페이지 또는 에러 페이지로
        }
        return "redirect:/register";
    }

    @PostMapping("/detail")
    public String processSignUp(@ModelAttribute("oAuth2RegistrationDto") OAuth2RegistrationDto request,
                         HttpSession session) {
        String socialId = (String) session.getAttribute("UNREGISTERED_SOCIAL_ID");
        String provider = (String) session.getAttribute("SOCIAL_TYPE");
        SocialType socialType = SocialType.from(provider);

        UserSignUpRequestDto newUser = UserSignUpRequestDto.builder()
                .socialId(socialId)
                .socialType(socialType)
                .nickname(request.getNickname())
                .gender(request.getGender())
                .age(request.getAge())
                .build();

        userService.createUser(newUser);

        return "redirect:/main";
    }

    @GetMapping("/nick/check")
    public ResponseEntity<?> checkNickDuplication(@RequestParam("nickname") String nickname) {
        boolean checkNickDuplication = userService.checkNickDuplication(nickname);

        if (checkNickDuplication) {
            return ResponseEntity.status(409).body("이미 사용중인 닉네임입니다.");
        } else {
            return ResponseEntity.ok("사용 가능한 닉네임입니다.");
        }
    }
}
