package com.kslj.mannam.domain.user.service;

import com.kslj.mannam.domain.user.dto.UserSignUpRequestDto;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public void save(UserSignUpRequestDto userSignUpRequestDto) {
        userRepository.save(userSignUpRequestDto.toUserEntity());
        log.info("nickname: {} 등록", userSignUpRequestDto.getNickname());
    }
}
