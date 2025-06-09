package com.kslj.mannam.domain.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SurveyLeaveDto {
    private Long sessionId;
    private String reasonCodes;
    private String customReason;
}
