package com.kslj.mannam.domain.match.dto;

import com.kslj.mannam.domain.review.dto.ReviewQueueDto;
import com.kslj.mannam.domain.user.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 매칭 요청 시 전달할 데이터
public class MatchQueueRequestDto {
    long userId;                    // 유저 ID
    Double latitude;                // 위도
    Double longitude;               // 경도
    String interests;               // 관심사
    Gender gender;                  // 성별
    Long age;                       // 나이
    String phone;                   // 전화번호
    int depressionScore;            // 우울 점수
    int efficacyScore;              // 자기 효능감 점수
    int relationshipScore;          // 대인관계 점수
    List<ReviewQueueDto> reviews;   // 해당 유저가 남긴 리뷰들
}
