package com.kslj.mannam.domain.user.dto;

import com.kslj.mannam.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class MyInfoResponseDto extends UserResponseDto {

    private String phone;

    public static MyInfoResponseDto fromEntity(User user) {
        UserResponseDto parentDto = UserResponseDto.fromEntity(user);

        return MyInfoResponseDto.builder()
                .userId(parentDto.getUserId())
                .socialType(parentDto.getSocialType())
                .nickname(parentDto.getNickname())
                .gender(parentDto.getGender())
                .latitude(parentDto.getLatitude())
                .longitude(parentDto.getLongitude())
                .age(parentDto.getAge())
                .interests(parentDto.getInterests())
                .footprint(parentDto.getFootprint())
                .question(parentDto.getQuestion())
                .questionList(parentDto.getQuestionList())
                .imgUrl(parentDto.getImgUrl())
                .maxAgeGap(parentDto.getMaxAgeGap())
                .allowOppositeGender(parentDto.getAllowOppositeGender())
                .maxMatchingDistance(parentDto.getMaxMatchingDistance())
                .phone(user.getPhone())
                .build();
    }
}
