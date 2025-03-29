package com.kslj.mannam.oauth2.service;

import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.repository.UserRepository;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String socialId) throws UsernameNotFoundException {
        User user = userRepository.findBySocialId(socialId)
                .orElseThrow(() -> new UsernameNotFoundException("Not Found: " + socialId));

        return new UserDetailsImpl(user);
    }
}
