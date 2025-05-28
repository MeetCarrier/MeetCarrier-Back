package com.kslj.mannam.domain.test.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestRequestDto {
    private Integer depressionScore;
    private Integer efficacyScore;
    private Integer relationshipScore;
}
