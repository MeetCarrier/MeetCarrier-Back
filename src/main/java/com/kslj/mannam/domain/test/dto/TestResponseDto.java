package com.kslj.mannam.domain.test.dto;

import com.kslj.mannam.domain.test.entity.Test;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestResponseDto {
    int depressionScore;
    int efficacyScore;
    int relationshipScore;
    LocalDateTime createdAt;

    public static TestResponseDto fromEntity(Test test) {
        return TestResponseDto.builder()
                .depressionScore(test.getDepressionScore())
                .efficacyScore(test.getEfficacyScore())
                .relationshipScore(test.getRelationshipScore())
                .createdAt(test.getCreatedAt())
                .build();
    }
}
