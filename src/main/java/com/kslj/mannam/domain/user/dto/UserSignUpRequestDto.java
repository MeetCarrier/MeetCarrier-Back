package com.kslj.mannam.domain.user.dto;

import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.enums.Gender;
import com.kslj.mannam.domain.user.enums.SocialType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSignUpRequestDto {
    private String socialId;
    private SocialType socialType;
    private String nickname;
    private Gender gender;
    private Long age;

    @Builder.Default
    private float footprint = 36.5f;

    public User toUserEntity() {
        User newUser = User.builder()
                .socialId(this.socialId)
                .socialType(this.socialType)
                .nickname(this.nickname)
                .gender(this.gender)
                .age(this.age)
                .footprint(this.footprint)
                .build();

        return newUser;
    }
}
