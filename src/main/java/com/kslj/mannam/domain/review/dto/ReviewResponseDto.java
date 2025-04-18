package com.kslj.mannam.domain.review.dto;

import com.kslj.mannam.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponseDto {
    long reviewId;
    int rating;
    String content;
    LocalDateTime createdAt;
    User reviewer;
}
