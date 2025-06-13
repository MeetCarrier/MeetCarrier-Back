package com.kslj.mannam.oauth2.controller;

import com.kslj.mannam.domain.user.dto.UserSignUpRequestDto;
import com.kslj.mannam.domain.user.enums.SocialType;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.oauth2.dto.OAuth2RegistrationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "회원가입 API", description = "OAuth2 로그인 사용자의 회원가입 절차 처리 API")
public class OAuthController {

    private final UserService userService;

    @GetMapping("/detail")
    public String showSignUpForm(HttpSession session) {
        if (session.getAttribute("UNREGISTERED_SOCIAL_ID") == null || session.getAttribute("SOCIAL_TYPE") == null) {
            return "redirect:/Login";
        }
        return "redirect:/register";
    }

    @Operation(
            summary = "OAuth2 회원가입 처리",
            description = "OAuth2 로그인 사용자의 회원가입 정보를 받아 새로운 유저를 생성하고 메인 페이지로 리디렉션합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회원가입 요청 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = OAuth2RegistrationDto.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "302", description = "회원가입 성공 후 메인 페이지로 리디렉션")
            }
    )
    @PostMapping("/detail")
    public String processSignUp(@RequestBody OAuth2RegistrationDto request,
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

    @Operation(
            summary = "닉네임 중복 검사",
            description = "회원가입 시 입력한 닉네임이 중복되는지 확인합니다.",
            parameters = {
                    @Parameter(
                            name = "nickname",
                            in = ParameterIn.QUERY,
                            description = "검사할 닉네임",
                            required = true,
                            schema = @Schema(type = "string")
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "사용 가능한 닉네임입니다."),
                    @ApiResponse(responseCode = "409", description = "이미 사용 중인 닉네임입니다.")
            }
    )
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
