package com.kslj.mannam.oauth2.controller;

import com.kslj.mannam.domain.user.dto.UserResponseDto;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import com.kslj.mannam.oauth2.service.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/auth/test")
@RequiredArgsConstructor
public class TestAuthController {

    private final UserDetailsServiceImpl userDetailsService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<String> loginAsTestUser(HttpServletRequest request, HttpServletResponse response) {
        UserDetails userDetails = userDetailsService.loadUserByUsername("testUser");

        UsernamePasswordAuthenticationToken authentication
                = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
        repo.saveContext(context, request, response);

        return ResponseEntity.ok("Logged in as testUser");
    }

    @PostMapping("/login2")
    public ResponseEntity<String> loginAsTestUser2(HttpServletRequest request, HttpServletResponse response) {
        UserDetails userDetails = userDetailsService.loadUserByUsername("testUser2");

        UsernamePasswordAuthenticationToken authentication
                = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
        repo.saveContext(context, request, response);

        return ResponseEntity.ok("Logged in as testUser2");
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.inspectUserDetails(userDetails);
        UserResponseDto response = UserResponseDto.fromEntity(userDetails.getUser());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/page")
    public String showLoginPage() {
        return "test-login"; // resources/templates/test-login.html
    }
}
