package com.kslj.mannam.domain.report.controller;

import com.kslj.mannam.domain.report.dto.ReportRequestDto;
import com.kslj.mannam.domain.report.dto.ReportResponseDto;
import com.kslj.mannam.domain.report.entity.Report;
import com.kslj.mannam.domain.report.service.ReportReplyService;
import com.kslj.mannam.domain.report.service.ReportService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ReportController {

    private final ReportService reportService;

    // 신고 내역 조회
    @GetMapping("/reports")
    public ResponseEntity<List<Report>> findAllReports(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<Report> reportList = reportService.getReports(userDetails.getUser());

        return ResponseEntity.ok(reportList);
    }

    // 신고 상세 정보 조회
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ReportResponseDto> findReportById(@PathVariable(value = "reportId") long reportId) {
        ReportResponseDto responseDto = reportService.getReportDetail(reportId);

        return ResponseEntity.ok(responseDto);
    }

    // 신고 등록
    @PostMapping("/reports/register")
    public ResponseEntity<?> createReport(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                          @RequestBody ReportRequestDto requestDto) {
        long reportId = reportService.createReport(userDetails.getUser(), requestDto);

        return ResponseEntity.ok("신고가 등록되었습니다. reportId = " + reportId);
    }

    // 신고 삭제
    @DeleteMapping("/reports/{reportId}")
    public ResponseEntity<?> deleteReport(@PathVariable(value = "reportId") long reportId) {
        long deleteReportId = reportService.deleteReport(reportId);

        return ResponseEntity.ok("신고가 삭제되었습니다. reportId = " + deleteReportId);
    }
}
