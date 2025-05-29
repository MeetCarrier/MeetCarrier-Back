package com.kslj.mannam.domain.report.dto;

import com.kslj.mannam.domain.report.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDto {
    private ReportType reportType;
    private String reportContent;
    private String reportDescription;
    private List<String> reportImages;
}
