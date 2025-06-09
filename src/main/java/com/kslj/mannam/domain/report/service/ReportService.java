package com.kslj.mannam.domain.report.service;

import com.kslj.mannam.domain.report.dto.ReportListDto;
import com.kslj.mannam.domain.report.dto.ReportRequestDto;
import com.kslj.mannam.domain.report.dto.ReportResponseDto;
import com.kslj.mannam.domain.report.entity.Report;
import com.kslj.mannam.domain.report.repository.ReportRepository;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserService userService;

    // 신고 작성
    @Transactional
    public long createReport(User reporter, ReportRequestDto reportRequestDto) {
        Report.ReportBuilder reportBuilder = Report.builder()
                .type(reportRequestDto.getReportType())
                .content(reportRequestDto.getReportContent())
                .description(reportRequestDto.getReportDescription())
                .reporter(reporter);

        if (reportRequestDto.getTargetUserId() != null) {
            User targetUser = userService.getUserById(reportRequestDto.getTargetUserId());
            reportBuilder.targetUser(targetUser);
        }

        // Report 객체 생성
        Report newReport = reportBuilder.build();

        Report savedReport = reportRepository.save(newReport);
        return savedReport.getId();
    }

    // 신고 리스트 불러오기
    @Transactional(readOnly = true)
    public List<ReportListDto> getReports(User user) {
        List<Report> reports = reportRepository.findAllByReporter(user);

        return reports.stream()
                .map(ReportListDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 신고 상세 정보 확인
    public ReportResponseDto getReportDetail(long reportId) {

        // 넘겨준 reportId를 바탕으로 Report 조회
        Optional<Report> optionalReport = reportRepository.findById(reportId);
        if(optionalReport.isEmpty()) {
            throw new EntityNotFoundException("신고 내역을 찾을 수 없습니다. reportId = " + reportId);
        }

        // 조회한 데이터를 바탕으로 응답 데이터 생성
        Report report = optionalReport.get();

        return ReportResponseDto.builder()
                .id(report.getId())
                .reportType(report.getType())
                .reportStatus(report.getStatus())
                .reportedAt(report.getReportedAt())
                .reportContent(report.getContent())
                .reportDescription(report.getDescription())
                .reporterId(report.getReporter().getId())
                .targetUserId(report.getTargetUser().getId())
                .build();
    }

    // 신고 수정


    // 신고 삭제
    public long deleteReport(long reportId) {

        Optional<Report> optionalReport = reportRepository.findById(reportId);
        if(optionalReport.isEmpty()) {
            throw new EntityNotFoundException("신고 내역을 찾을 수 없습니다. reportId = " + reportId);
        }

        reportRepository.deleteById(reportId);

        return reportId;
    }
}
