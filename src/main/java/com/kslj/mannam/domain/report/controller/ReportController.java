package com.kslj.mannam.domain.report.controller;

import com.kslj.mannam.domain.report.dto.ReportListDto;
import com.kslj.mannam.domain.report.dto.ReportRequestDto;
import com.kslj.mannam.domain.report.dto.ReportResponseDto;
import com.kslj.mannam.domain.report.entity.Report;
import com.kslj.mannam.domain.report.service.ReportService;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller("/reports")
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;

    // 신고 내역 조회
    @GetMapping("")
    public ResponseEntity<List<ReportListDto>> findAllReports(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<ReportListDto> reportList = reportService.getReports(userService.getUserById(1));

        return ResponseEntity.ok(reportList);
    }

    // 신고 상세 정보 조회
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponseDto> findReportById(@PathVariable(value = "reportId") long reportId) {
        ReportResponseDto responseDto = reportService.getReportDetail(reportId);

        return ResponseEntity.ok(responseDto);
    }

    // 신고 등록
    @PostMapping("/register")
    public ResponseEntity<?> createReport(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                          @RequestBody ReportRequestDto requestDto) {
        long reportId = reportService.createReport(userService.getUserById(1), requestDto);

        return ResponseEntity.ok("신고가 등록되었습니다. reportId = " + reportId);
    }

    // 신고 삭제
    @DeleteMapping("/{reportId}")
    public ResponseEntity<?> deleteReport(@PathVariable(value = "reportId") long reportId) {
        long deleteReportId = reportService.deleteReport(reportId);

        return ResponseEntity.ok("신고가 삭제되었습니다. reportId = " + deleteReportId);
    }
}
