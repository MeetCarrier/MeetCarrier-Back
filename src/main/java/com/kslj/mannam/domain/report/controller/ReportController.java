package com.kslj.mannam.domain.report.controller;

import com.kslj.mannam.domain.report.dto.ReportListDto;
import com.kslj.mannam.domain.report.dto.ReportRequestDto;
import com.kslj.mannam.domain.report.dto.ReportResponseDto;
import com.kslj.mannam.domain.report.entity.Report;
import com.kslj.mannam.domain.report.service.ReportService;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@Tag(name = "신고", description = "신고 관리 API")
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;

    // 신고 내역 조회
    @GetMapping
    @Operation(
            summary     = "신고 내역 조회",
            description = "현재 로그인한 사용자가 등록한 모든 신고 내역을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "조회 성공",
                            content      = @Content(
                                    mediaType = "application/json",
                                    array     = @ArraySchema(
                                            schema = @Schema(implementation = ReportListDto.class)
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<List<ReportListDto>> findAllReports(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<ReportListDto> reportList = reportService.getReports(userService.getUserById(1));

        return ResponseEntity.ok(reportList);
    }

    // 신고 상세 정보 조회
    @GetMapping("/{reportId}")
    @Operation(
            summary     = "신고 상세 조회",
            description = "지정된 ID의 신고 상세 정보를 조회합니다.",
            parameters = {
                    @Parameter(
                            name        = "reportId",
                            description = "조회할 신고의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "조회 성공",
                            content      = @Content(
                                    mediaType = "application/json",
                                    schema      = @Schema(implementation = ReportResponseDto.class)
                            )
                    )
            }
    )
    public ResponseEntity<ReportResponseDto> findReportById(@PathVariable(value = "reportId") long reportId) {
        ReportResponseDto responseDto = reportService.getReportDetail(reportId);

        return ResponseEntity.ok(responseDto);
    }

    // 신고 등록
    @PostMapping("/register")
    @Operation(
            summary     = "신고 등록",
            description = "새로운 신고를 등록합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "신고 등록 요청 DTO",
                    required    = true,
                    content     = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(implementation = ReportRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "등록 성공",
                            content      = @Content(
                                    mediaType = "text/plain",
                                    schema    = @Schema(type = "string")
                            )
                    )
            }
    )
    public ResponseEntity<?> createReport(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                          @RequestBody ReportRequestDto requestDto) {
        long reportId = reportService.createReport(userService.getUserById(1), requestDto);

        return ResponseEntity.ok("신고가 등록되었습니다. reportId = " + reportId);
    }

    // 신고 삭제
    @DeleteMapping("/{reportId}")
    @Operation(
            summary     = "신고 삭제",
            description = "지정된 ID의 신고를 삭제합니다.",
            parameters = {
                    @Parameter(
                            name        = "reportId",
                            description = "삭제할 신고의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "삭제 성공",
                            content      = @Content(
                                    mediaType = "text/plain",
                                    schema    = @Schema(type = "string")
                            )
                    )
            }
    )
    public ResponseEntity<?> deleteReport(@PathVariable(value = "reportId") long reportId) {
        long deleteReportId = reportService.deleteReport(reportId);

        return ResponseEntity.ok("신고가 삭제되었습니다. reportId = " + deleteReportId);
    }
}
