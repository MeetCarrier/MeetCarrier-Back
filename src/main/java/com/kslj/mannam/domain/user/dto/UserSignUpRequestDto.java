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
    private String region;
    private Long age;
    private String personalities;
    private String preferences;
    private String interests;

    @Builder.Default
    private float footprint = 36.5f;

    public User toUserEntity() {
        User newUser = User.builder()
                .socialId(this.socialId)
                .socialType(this.socialType)
                .nickname(this.nickname)
                .gender(this.gender)
                .region(this.region)
                .age(this.age)
                .personalities(this.personalities)
                .preferences(this.preferences)
                .interests(this.interests)
                .footprint(this.footprint)
                .build();

        return newUser;
    }
}
