package com.kslj.mannam.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewByReviewerIdDto {
    long reviewId;
    int rating;
    String content;
    int step;
    LocalDateTime createdAt;
    long userId;
    String userName;
}
