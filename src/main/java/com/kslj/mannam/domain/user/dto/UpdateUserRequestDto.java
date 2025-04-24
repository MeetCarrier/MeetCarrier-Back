package com.kslj.mannam.domain.user.dto;

import com.kslj.mannam.domain.user.enums.Gender;
import lombok.Data;

@Data
public class UpdateUserRequestDto {
    String nickname;
    Gender gender;
    String region;
    String personalities;
    String preferences;
    String interests;
    String footprint;
    String questions;
    String imgUrl;
    String phone;
}
