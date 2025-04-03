package com.kslj.mannam.domain.match.dto;

import com.kslj.mannam.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchRequestDto {
    int score;
    User user1;
    User user2;
}
