package com.kslj.mannam.domain.survey.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SurveyQuestionResponseDto {
    private long questionId;
    private String content;
}
