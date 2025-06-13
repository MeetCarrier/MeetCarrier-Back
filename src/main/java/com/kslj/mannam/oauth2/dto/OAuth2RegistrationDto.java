package com.kslj.mannam.oauth2.dto;

import com.kslj.mannam.domain.user.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2RegistrationDto {
    private String nickname;
    private Gender gender;
    private Long age;
}
