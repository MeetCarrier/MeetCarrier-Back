package com.kslj.mannam.domain.report.dto;

import com.kslj.mannam.domain.report.enums.ReportStatus;
import com.kslj.mannam.domain.report.enums.ReportType;
import com.kslj.mannam.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponseDto {
    // 신고
    private long id;
    private ReportType reportType;
    private ReportStatus reportStatus;
    private LocalDateTime reportedAt;
    private String reportContent;
    private User reporter;

    // 신고 이미지
    private List<String> reportImages;
}
