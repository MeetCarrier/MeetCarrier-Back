package com.kslj.mannam.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewQueueDto {
    int rating;     // 리뷰 점수
    long userId;    // 상대방 ID
}
