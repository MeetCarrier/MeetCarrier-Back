package com.kslj.mannam.domain.user.dto;

import com.kslj.mannam.domain.user.enums.Gender;
import lombok.Data;

@Data
public class UpdateUserRequestDto {
    String nickname;
    Gender gender;
    Double latitude;
    Double longitude;
    Long age;
    String interests;
    String question;
    String questionList;
    String imgUrl;
    String phone;
    Integer maxAgeGap;
    Boolean allowOppositeGender;
    Double maxMatchingDistance;
}
