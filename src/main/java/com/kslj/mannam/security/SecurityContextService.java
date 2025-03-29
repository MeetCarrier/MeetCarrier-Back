package com.kslj.mannam.security;

import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import com.kslj.mannam.oauth2.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityContextService {
    private final UserDetailsServiceImpl userDetailsService;

    public void refreshUserDetails(String socialId) {
        UserDetailsImpl updatedUserDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(socialId);

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUserDetails,
                updatedUserDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }
}
