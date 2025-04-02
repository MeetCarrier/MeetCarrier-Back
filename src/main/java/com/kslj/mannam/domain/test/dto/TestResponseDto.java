package com.kslj.mannam.domain.test.dto;

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
    int relationshipScore;
    LocalDateTime createdAt;
}
