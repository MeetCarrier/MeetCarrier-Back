package com.kslj.mannam.domain.user.service;

import com.kslj.mannam.domain.user.dto.UpdateUserRequestDto;
import com.kslj.mannam.domain.user.dto.UserSignUpRequestDto;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.repository.UserRepository;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
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
    public void signUpOrRejoin(UserSignUpRequestDto requestDto) {
        Optional<User> userOpt = userRepository.findBySocialId(requestDto.getSocialId());

        if (userOpt.isPresent()) {
            // ✅ DB에 유저가 존재 -> 재가입 처리
            User existingUser = userOpt.get();
            if (existingUser.isDeleted()) {
                existingUser.rejoin(requestDto); // Dirty checking에 의해 업데이트됨
            } else {
                throw new IllegalStateException("이미 가입된 활성 사용자입니다.");
            }
        } else {
            // ❌ DB에 유저가 없음 -> 신규 가입 처리 (단순화된 createUser 호출)
            createUser(requestDto);
        }
    }


    @Transactional
    public long createUser(UserSignUpRequestDto dto) {
        User newUser = dto.toUserEntity();
        User savedUser = userRepository.save(newUser);

        log.info("nickname: {} 신규 등록", dto.getNickname());
        return savedUser.getId();
    }

    @Cacheable(value = "userCache", key = "#id", unless = "#result == null")
    @Transactional(readOnly = true)
    public User getUserById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자가 존재하지 않습니다."));
    }

    @Transactional(readOnly = true)
    public User getUserBySocialId(String socialId) {
        return userRepository.findBySocialId(socialId).orElseThrow();
    }

    public Boolean checkNickDuplication(String nickname) {
        Optional<User> optionalUser = userRepository.findByNickname(nickname);

        return optionalUser.isPresent();
    }

    @CachePut(value = "userCache", key = "#userDetails.id")
    @Transactional
    public User updateUser(UserDetailsImpl userDetails, UpdateUserRequestDto dto) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다"));

        if (dto.getNickname() != null) user.updateNickname(dto.getNickname());
        if (dto.getGender() != null) user.updateGender(dto.getGender());
        if (dto.getLatitude() != null) user.updateLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) user.updateLongitude(dto.getLongitude());
        if (dto.getAge() != null) user.updateAge(dto.getAge());
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

        return user;
    }

    @CacheEvict(value = "userCache", key = "#user.id")
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

    public void inspectUserDetails(UserDetailsImpl userDetails) {
        if (userDetails == null)
            throw new AccessDeniedException("로그인된 유저 정보가 없습니다.");
    }
}
