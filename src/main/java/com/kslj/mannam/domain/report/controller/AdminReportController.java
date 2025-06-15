package com.kslj.mannam.domain.report.controller;

import com.kslj.mannam.domain.report.dto.ReportListDto;
import com.kslj.mannam.domain.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // 관리자 권한 확인용
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
@Tag(name = "관리자-신고", description = "관리자용 신고 관리 API")
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final ReportService reportService;

    // 1. 모든 신고 조회
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Spring Security를 통해 ADMIN 역할을 가진 사용자만 접근 가능하도록 설정
    @Operation(
            summary = "모든 신고 내역 조회",
            description = "관리자가 모든 사용자의 신고 내역을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = ReportListDto.class)
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<List<ReportListDto>> findAllReportsForAdmin() {
        List<ReportListDto> reportList = reportService.getAllReports();
        return ResponseEntity.ok(reportList);
    }
}