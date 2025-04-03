package com.kslj.mannam.user;

import com.kslj.mannam.domain.user.dto.UserSignUpRequestDto;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.enums.Gender;
import com.kslj.mannam.domain.user.enums.SocialType;
import com.kslj.mannam.domain.user.repository.UserRepository;
import com.kslj.mannam.domain.user.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional  // 테스트 후 자동 롤백
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // 회원 등록 및 조회 테스트
    @Test
    public void testCreateAndFindUser() {
        // given
        UserSignUpRequestDto signUpRequestDto = UserSignUpRequestDto.builder()
                .socialId("1234")
                .socialType(SocialType.Google)
                .nickname("Mannam")
                .gender(Gender.Male)
                .region("서울")
                .personalities("스포츠")
                .preferences("게임")
                .interests("롤")
                .build();

        // when
        userService.createUser(signUpRequestDto);
        User foundUser = userService.getUserBySocialId(signUpRequestDto.getSocialId());

        // then
        Assertions.assertThat(foundUser.getNickname()).isEqualTo(signUpRequestDto.getNickname());
    }

    // 닉네임 중복 테스트
    @Test
    public void testDuplicateUser() {
        // given
        UserSignUpRequestDto signUpRequestDto = UserSignUpRequestDto.builder()
                .socialId("1234")
                .socialType(SocialType.Google)
                .nickname("Mannam")
                .gender(Gender.Male)
                .region("서울")
                .personalities("스포츠")
                .preferences("게임")
                .interests("롤")
                .build();

        userService.createUser(signUpRequestDto);

        // when
        boolean result = userService.checkNickDuplication("Mannam");

        // then
        Assertions.assertThat(result).isTrue();
    }
}
