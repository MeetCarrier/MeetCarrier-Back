package com.kslj.mannam.domain.report.dto;

import com.kslj.mannam.domain.report.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDto {
    private ReportType reportType;
    private Long targetUserId;
    private String reportContent;
    private String reportDescription;
}
