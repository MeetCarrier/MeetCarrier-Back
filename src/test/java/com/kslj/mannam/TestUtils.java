package com.kslj.mannam;

import com.kslj.mannam.domain.user.dto.UserSignUpRequestDto;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.enums.Gender;
import com.kslj.mannam.domain.user.enums.SocialType;
import com.kslj.mannam.domain.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestUtils {

    @Autowired
    private UserService userService;

    // 유저 생성 메서드
    public User createAndGetTestUser() {
        UserSignUpRequestDto signUpRequestDto = UserSignUpRequestDto.builder()
                .socialId("1234")
                .socialType(SocialType.Google)
                .nickname("Mannam")
                .gender(Gender.Male)
                .age(23L)
                .personalities("스포츠")
                .interests("롤")
                .build();

        long userId = userService.createUser(signUpRequestDto);
        return userService.getUserById(userId);
    }
}
