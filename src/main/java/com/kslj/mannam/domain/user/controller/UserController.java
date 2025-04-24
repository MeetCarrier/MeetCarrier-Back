package com.kslj.mannam.domain.user.controller;

import com.kslj.mannam.domain.user.dto.UpdateUserRequestDto;
import com.kslj.mannam.domain.user.dto.UserResponseDto;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.bootstrap.HttpServer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;

    @GetMapping("/user")
    public ResponseEntity<?> getUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(UserResponseDto.fromEntity(userDetails.getUser()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable(value="userId") long userId) {
        User user = userService.getUserById(userId);

        if (user.isDeleted()) {
            return ResponseEntity.status(HttpStatus.GONE).body("탈퇴한 회원입니다.");
        }

        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    @PatchMapping("/user")
    public ResponseEntity<?> updateUser(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @RequestBody UpdateUserRequestDto dto) {
        userService.updateUser(userDetails.getUser(), dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        request.getSession().invalidate();                     // 세션 무효화
        SecurityContextHolder.clearContext();                  // SecurityContext 초기화
        return ResponseEntity.ok("로그아웃 완료");
    }

    @DeleteMapping("/user/withdraw")
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
