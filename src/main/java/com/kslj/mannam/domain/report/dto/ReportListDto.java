package com.kslj.mannam.domain.report.dto;

import com.kslj.mannam.domain.report.entity.Report;
import com.kslj.mannam.domain.report.enums.ReportStatus;
import com.kslj.mannam.domain.report.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportListDto {
    long reportId;
    ReportType type;
    ReportStatus status;
    String content;
    LocalDateTime reportedAt;
    String reporterNickname;

    public static ReportListDto fromEntity(Report report) {
        return ReportListDto.builder()
                .reportId(report.getId())
                .type(report.getType())
                .status(report.getStatus())
                .content(report.getContent())
                .reportedAt(report.getReportedAt())
                .reporterNickname(report.getUser().getNickname())
                .build();
    }
}
