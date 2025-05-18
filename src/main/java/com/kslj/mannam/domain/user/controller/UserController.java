package com.kslj.mannam.domain.user.controller;

import com.kslj.mannam.domain.user.dto.UpdateUserRequestDto;
import com.kslj.mannam.domain.user.dto.UserResponseDto;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@Tag(name = "유저", description = "유저 관리 API")
@RequestMapping("/user")
@Controller
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "유저 정보 조회",
            description = "현재 로그인 중인 유저의 정보를 조회합니다.<br><br>" +
                    "<b>Gender (성별)</b><br>" +
                    "- Male: 남성<br>" +
                    "- Female: 여성<br><br>" +
                    "<b>SocialType (소셜 로그인 타입)</b><br>" +
                    "- Kakao: 카카오 로그인<br>" +
                    "- Google: 구글 로그인"
    )
    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = UserResponseDto.class)))
    public ResponseEntity<?> getUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        // 테스트 용도
        // User testUser = userService.getUserById(1);

        return ResponseEntity.ok(UserResponseDto.fromEntity(userDetails.getUser()));
        // return ResponseEntity.ok(UserResponseDto.fromEntity(testUser));
    }

    @GetMapping("/{userId}")
    @Operation(
            summary = "특정 유저 정보 조회",
            description = "전달된 userId로 특정 유저의 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "410", description = "조회한 유저가 탈퇴한 유저"),
            },
            parameters = {
                @Parameter(name = "userId", description = "조회할 유저의 ID", required = true, example = "1")
            })
    public ResponseEntity<?> getUserById(@PathVariable("userId") long userId) {
        User user = userService.getUserById(userId);

        if (user.isDeleted()) {
            return ResponseEntity.status(HttpStatus.GONE).body("탈퇴한 회원입니다.");
        }

        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    @PatchMapping
    @Operation(
            summary = "유저 정보 수정",
            description = "전달된 정보들로 데이터베이스에 저장된 유저의 정보를 수정합니다.\n요청 전송 시 필요한 부분의 데이터만 채워서 보내면 됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 완료")
            }
    )
    public ResponseEntity<?> updateUser(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @RequestBody UpdateUserRequestDto dto) {
//        // 테스트 용도
//        User testUser = userService.getUserById(1);
//        userService.updateUser(testUser, dto);

        userService.updateUser(userDetails.getUser(), dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃",
            description = "현재 로그인된 유저를 로그아웃 처리합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그아웃 완료")
            }
    )
    public ResponseEntity<?> logout(HttpServletRequest request) {
        request.getSession().invalidate();                     // 세션 무효화
        SecurityContextHolder.clearContext();                  // SecurityContext 초기화
        return ResponseEntity.ok("로그아웃 완료");
    }

    @DeleteMapping("/withdraw")
    @Operation(
            summary = "유저 탈퇴",
            description = "현재 로그인된 유저를 탈퇴 처리합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "탈퇴 처리 완료")
            }
    )
    public ResponseEntity<?> withdrawUser(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {
        userService.withdrawUser(userDetails.getUser());

        // 세션 무효화
        request.getSession().invalidate();

        // SecurityContext 초기화
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok().build();
    }
}
