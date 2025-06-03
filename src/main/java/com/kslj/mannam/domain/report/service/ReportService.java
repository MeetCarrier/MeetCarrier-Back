package com.kslj.mannam.domain.report.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kslj.mannam.domain.report.dto.ReportListDto;
import com.kslj.mannam.domain.report.dto.ReportRequestDto;
import com.kslj.mannam.domain.report.dto.ReportResponseDto;
import com.kslj.mannam.domain.report.entity.Report;
import com.kslj.mannam.domain.report.repository.ReportRepository;
import com.kslj.mannam.domain.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ObjectMapper objectMapper;

    // 신고 작성
    @Transactional
    public long createReport(User user, ReportRequestDto reportRequestDto) {
        String imageJson;

        // List로 넘어온 이미지들 문자열로 변환
        try {
            imageJson = objectMapper.writeValueAsString(reportRequestDto.getReportImages());
        } catch (Exception e) {
            throw new RuntimeException("이미지 리스트 -> 문자열 변환 실패", e);
        }

        // Report 객체 생성
        Report newReport = Report.builder()
                .type(reportRequestDto.getReportType())
                .content(reportRequestDto.getReportContent())
                .description(reportRequestDto.getReportDescription())
                .user(user)
                .images(imageJson)
                .build();

        for (String image : reportRequestDto.getReportImages()) {
            // 업로드된 이미지들 경로 변경 코드 필요
        }

        Report savedReport = reportRepository.save(newReport);
        return savedReport.getId();
    }

    // 신고 리스트 불러오기
    @Transactional(readOnly = true)
    public List<ReportListDto> getReports(User user) {
        List<Report> reports = reportRepository.findAllByUser(user);

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

        List<String> images = new ArrayList<>();
        try {
            images = objectMapper.readValue(report.getImages(), new TypeReference<>() {});
        } catch(Exception e) {
            throw new RuntimeException("이미지 문자열 -> 리스트 변환 실패", e);
        }

        return ReportResponseDto.builder()
                .id(report.getId())
                .reportType(report.getType())
                .reportStatus(report.getStatus())
                .reportedAt(report.getReportedAt())
                .reportContent(report.getContent())
                .reportDescription(report.getDescription())
                .reporterId(report.getUser().getId())
                .reporterNickname(report.getUser().getNickname())
                .reportImages(images)
                .build();
    }

    // 신고 수정


    // 신고 삭제
    public long deleteReport(long reportId) {

        Optional<Report> optionalReport = reportRepository.findById(reportId);
        if(optionalReport.isEmpty()) {
            throw new EntityNotFoundException("신고 내역을 찾을 수 없습니다. reportId = " + reportId);
        }

        // 삭제하기전에 S3에 올라간 이미지 삭제 코드 필요

        reportRepository.deleteById(reportId);

        return reportId;
    }
}
