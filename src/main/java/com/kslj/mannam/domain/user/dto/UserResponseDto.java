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
public class UserResponseDto {
    long userId;
    SocialType socialType;
    String nickname;
    Gender gender;
    String region;
    String personalities;
    String preferences;
    String interests;
    float footprint;
    String questions;
    String imgUrl;

    public static UserResponseDto fromEntity(User user) {
        return UserResponseDto.builder()
                .userId(user.getId())
                .socialType(user.getSocialType())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .region(user.getRegion())
                .personalities(user.getPersonalities())
                .preferences(user.getPreferences())
                .interests(user.getInterests())
                .footprint(user.getFootprint())
                .questions(user.getQuestions())
                .imgUrl(user.getImgUrl())
                .build();
    }
}