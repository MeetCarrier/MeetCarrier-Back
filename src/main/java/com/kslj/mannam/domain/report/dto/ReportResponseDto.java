package com.kslj.mannam.domain.report.dto;

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
public class ReportResponseDto {
    // 신고
    Long id;
    ReportType reportType;
    ReportStatus reportStatus;
    LocalDateTime reportedAt;
    String reportContent;
    String reportDescription;
    Long reporterId;
    Long targetUserId;
    String replyContent;
    LocalDateTime repliedAt;
}
