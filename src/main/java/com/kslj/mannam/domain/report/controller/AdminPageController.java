package com.kslj.mannam.domain.report.controller;

import com.kslj.mannam.domain.report.dto.ReportListDto;
import com.kslj.mannam.domain.report.dto.ReportResponseDto;
import com.kslj.mannam.domain.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin") // 관리자 페이지는 /admin 경로 아래에 묶습니다.
public class AdminPageController {

    private final ReportService reportService;

    /**
     * 관리자용 신고 목록 페이지를 보여주는 메서드
     */
    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public String getReportListPage(Model model) {
        // ReportService를 통해 모든 신고 목록을 가져온다.
        List<ReportListDto> reports = reportService.getAllReports();

        // Model 객체에 신고 목록을 담아 View(HTML)로 전달한다.
        model.addAttribute("reports", reports);

        // resources/templates/admin/report-list.html 파일을 렌더링한다.
        return "admin/report-list";
    }

    /**
     * 관리자용 신고 상세 및 답변 페이지를 보여주는 메서드
     */
    @GetMapping("/reports/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String getReportDetailPage(@PathVariable long reportId, Model model) {
        // reportId로 특정 신고의 상세 정보를 가져온다.
        ReportResponseDto report = reportService.getReportDetail(reportId);

        // Model에 상세 정보를 담아 View로 전달한다.
        model.addAttribute("report", report);

        // resources/templates/admin/report-detail.html 파일을 렌더링한다.
        return "admin/report-detail";
    }
}