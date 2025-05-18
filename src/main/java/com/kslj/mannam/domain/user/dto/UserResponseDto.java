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
    Double latitude;
    Double longitude;
    Long age;
    String personalities;
    String interests;
    Double footprint;
    String question;
    String questionList;
    String imgUrl;
    Integer maxAgeGap;
    Boolean allowOppositeGender;
    Double maxMatchingDistance;

    public static UserResponseDto fromEntity(User user) {
        return UserResponseDto.builder()
                .userId(user.getId())
                .socialType(user.getSocialType())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .age(user.getAge())
                .personalities(user.getPersonalities())
                .interests(user.getInterests())
                .footprint(user.getFootprint())
                .question(user.getQuestion())
                .questionList(user.getQuestionList())
                .imgUrl(user.getImgUrl())
                .maxAgeGap(user.getMaxAgeGap())
                .allowOppositeGender(user.isAllowOppositeGender())
                .maxMatchingDistance(user.getMaxMatchingDistance())
                .build();
    }
}