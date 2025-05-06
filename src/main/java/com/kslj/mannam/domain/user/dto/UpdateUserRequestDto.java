package com.kslj.mannam.domain.user.dto;

import com.kslj.mannam.domain.user.enums.Gender;
import lombok.Data;

@Data
public class UpdateUserRequestDto {
    String nickname;
    Gender gender;
    String region;
    Long age;
    String personalities;
    String interests;
    String questions;
    String imgUrl;
    String phone;
    Integer maxAgeGap;
    Boolean allowOppositeGender;
    Double maxMatchingDistance;
}
