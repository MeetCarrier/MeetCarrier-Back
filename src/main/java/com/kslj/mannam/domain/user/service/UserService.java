package com.kslj.mannam.domain.user.service;

import com.kslj.mannam.domain.user.dto.UserSignUpRequestDto;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public long createUser(UserSignUpRequestDto userSignUpRequestDto) {
        User savedUser = userRepository.save(userSignUpRequestDto.toUserEntity());
        log.info("nickname: {} 등록", userSignUpRequestDto.getNickname());

        return savedUser.getId();
    }

    public User getUserById(long id) {
        return userRepository.findById(id).orElseThrow();
    }

    public User getUserBySocialId(String socialId) {
        return userRepository.findBySocialId(socialId).orElseThrow();
    }

    public Boolean checkNickDuplication(String nickname) {
        Optional<User> optionalUser = userRepository.findByNickname(nickname);

        return optionalUser.isPresent();
    }
}
