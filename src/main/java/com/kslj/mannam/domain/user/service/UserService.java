package com.kslj.mannam.domain.user.service;

import com.kslj.mannam.domain.user.dto.UpdateUserRequestDto;
import com.kslj.mannam.domain.user.dto.UserSignUpRequestDto;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.repository.UserRepository;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public long createUser(UserSignUpRequestDto dto) {
        Optional<User> existingUser = userRepository.findBySocialId(dto.getSocialId());

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (user.isDeleted()) {
                user.rejoin(dto);
                return user.getId();
            } else {
                throw new IllegalStateException("이미 가입된 회원입니다.");
            }
        }

        // 신규 회원가입
        User savedUser = userRepository.save(dto.toUserEntity());
        log.info("nickname: {} 등록", dto.getNickname());
        return savedUser.getId();
    }

    @Transactional(readOnly = true)
    public User getUserById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));
    }

    @Transactional(readOnly = true)
    public User getUserBySocialId(String socialId) {
        return userRepository.findBySocialId(socialId).orElseThrow();
    }

    public Boolean checkNickDuplication(String nickname) {
        Optional<User> optionalUser = userRepository.findByNickname(nickname);

        return optionalUser.isPresent();
    }

    @Transactional
    public void updateUser(UserDetailsImpl userDetails, UpdateUserRequestDto dto) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다"));

        if (dto.getNickname() != null) user.updateNickname(dto.getNickname());
        if (dto.getGender() != null) user.updateGender(dto.getGender());
        if (dto.getLatitude() != null) user.updateLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) user.updateLongitude(dto.getLongitude());
        if (dto.getAge() != null) user.updateAge(dto.getAge());
        if (dto.getPersonalities() != null) user.updatePersonalities(dto.getPersonalities());
        if (dto.getInterests() != null) user.updateInterests(dto.getInterests());
        if (dto.getQuestion() != null) user.updateQuestion(dto.getQuestion());
        if (dto.getQuestionList() != null) user.updateQuestionList(dto.getQuestionList());
        if (dto.getImgUrl() != null) user.updateImgUrl(dto.getImgUrl());
        if (dto.getPhone() != null) user.updatePhone(dto.getPhone());
        if (dto.getMaxAgeGap() != null) user.updateMaxAgeGap(dto.getMaxAgeGap());
        if (dto.getAllowOppositeGender() != null) user.updateAllowOppositeGender(dto.getAllowOppositeGender());
        if (dto.getMaxMatchingDistance() != null) user.updateMaxMatchingDistance(dto.getMaxMatchingDistance());

        Authentication auth = new UsernamePasswordAuthenticationToken(
                new UserDetailsImpl(user),  // 새로 만든 UserDetailsImpl
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Transactional
    public void withdrawUser(User user) {
        user.withdraw(); // 탈퇴 처리
    }

    public String getSmsCode() {
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            builder.append(random.nextInt(10)); // 0 ~ 9
        }
        return builder.toString();
    }
}
