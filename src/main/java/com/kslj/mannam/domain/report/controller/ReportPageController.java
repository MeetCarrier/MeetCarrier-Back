package com.kslj.mannam.domain.report.controller;

import com.kslj.mannam.domain.report.dto.ReportListDto;
import com.kslj.mannam.domain.report.dto.ReportResponseDto;
import com.kslj.mannam.domain.report.service.ReportService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/my/reports") // 예: 마이페이지 하위 경로
public class ReportPageController {

    private final ReportService reportService;

    // 1. 내 신고 목록 페이지
    @GetMapping
    public String getMyReportList(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        // 기존의 내 신고 목록 조회 서비스를 사용
        List<ReportListDto> reports = reportService.getReports(userDetails.getUser());
        model.addAttribute("reports", reports);
        return "my/report-list"; // 템플릿 경로
    }

    // 2. 내 신고 상세 페이지
    @GetMapping("/{reportId}")
    public String getMyReportDetail(@PathVariable long reportId,
                                    @AuthenticationPrincipal UserDetailsImpl userDetails,
                                    Model model) {
        ReportResponseDto report = reportService.getReportDetail(reportId);

        // [보안] 본인이 작성한 신고가 맞는지 확인
        if (!report.getReporterId().equals(userDetails.getUser().getId())) {
            throw new AccessDeniedException("본인의 신고 내역만 조회할 수 있습니다.");
        }

        model.addAttribute("report", report);
        return "my/report-detail"; // 템플릿 경로
    }
}