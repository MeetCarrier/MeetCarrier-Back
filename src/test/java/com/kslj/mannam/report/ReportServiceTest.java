package com.kslj.mannam.report;

import com.kslj.mannam.TestUtils;
import com.kslj.mannam.domain.report.dto.ReplyRequestDto;
import com.kslj.mannam.domain.report.dto.ReportRequestDto;
import com.kslj.mannam.domain.report.dto.ReportResponseDto;
import com.kslj.mannam.domain.report.entity.Report;
import com.kslj.mannam.domain.report.entity.ReportReply;
import com.kslj.mannam.domain.report.enums.ReportStatus;
import com.kslj.mannam.domain.report.enums.ReportType;
import com.kslj.mannam.domain.report.service.ReportReplyService;
import com.kslj.mannam.domain.report.service.ReportService;
import com.kslj.mannam.domain.user.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Transactional
public class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportReplyService reportReplyService;

    @Autowired
    TestUtils testUtils;

    // ReportRequestDto 생성 메서드
    private ReportRequestDto createReportRequest(String reportContent, List<String> reportImages) {
        return ReportRequestDto.builder()
                .reportType(ReportType.Question)
                .reportContent(reportContent)
                .reportImages(reportImages)
                .build();
    }

    // ReplyRequestDto 생성 메서드
    private ReplyRequestDto createReplyRequest(String replyContent) {
        return ReplyRequestDto.builder().content(replyContent).build();
    }

    // 신고 등록 및 조회 테스트 (이미지 X)
    @Test
    public void testCreateReport() {
        //given
        User foundUser = testUtils.createAndGetTestUser();
        ReportRequestDto reportRequestDto = createReportRequest("버그가 있어요.", new ArrayList<>());

        //when
        long savedReportId = reportService.createReport(foundUser, reportRequestDto);
        ReportResponseDto foundReport = reportService.getReportDetail(savedReportId);

        //then
        System.out.println("reportRequestDto.getReportContent() = " + reportRequestDto.getReportContent());
        System.out.println("foundReport.getReportContent() = " + foundReport.getReportContent());
        Assertions.assertEquals(foundReport.getReportContent(), reportRequestDto.getReportContent());
    }

    // 신고 등록 및 조회 테스트 (이미지 O)
    @Test
    public void testCreateReportWithImage() {
        //given
        User foundUser = testUtils.createAndGetTestUser();
        ReportRequestDto reportRequestDto = createReportRequest("버그가 있어요.", new ArrayList<>(List.of("report1.jpg, report2.jpg, report3.jpg")));

        //when
        long savedReportId = reportService.createReport(foundUser, reportRequestDto);
        ReportResponseDto foundReport = reportService.getReportDetail(savedReportId);

        //then
        System.out.println("reportRequestDto.getReportImages() = " + reportRequestDto.getReportImages());
        System.out.println("foundReport.getReportImages() = " + foundReport.getReportImages());
        Assertions.assertEquals(foundReport.getReportImages(), reportRequestDto.getReportImages());
    }

    // 신고 삭제 테스트
    @Test
    public void testDeleteReport() {
        //given
        User foundUser = testUtils.createAndGetTestUser();
        ReportRequestDto reportRequestDto1 = createReportRequest("버그가 있어요.", new ArrayList<>());
        ReportRequestDto reportRequestDto2 = createReportRequest("이상해요.", new ArrayList<>());
        ReportRequestDto reportRequestDto3 = createReportRequest("제대로 작동하지 않습니다.", new ArrayList<>());

        long savedReportId1 = reportService.createReport(foundUser, reportRequestDto1);
        long savedReportId2 = reportService.createReport(foundUser, reportRequestDto2);
        long savedReportId3 = reportService.createReport(foundUser, reportRequestDto3);

        //when
        reportService.deleteReport(savedReportId1);
        List<Report> reports = reportService.getReports(foundUser);

        //then
        Assertions.assertEquals(2, reports.size());
    }

    // 답변 등록 테스트
    @Test
    public void testCreateReportReply() {
        //given
        User reporter = testUtils.createAndGetTestUser();
        User replier = testUtils.createAndGetTestUser();

        ReportRequestDto reportRequestDto = createReportRequest("버그가 있어요.", new ArrayList<>());
        long reportId = reportService.createReport(reporter, reportRequestDto);

        ReplyRequestDto replyRequestDto = createReplyRequest("확인해볼게요.");

        //when
        long replyId = reportReplyService.createReportReply(reportId, replyRequestDto, replier);
        ReportReply foundReply = reportReplyService.getReportReply(replyId);

        //then
        System.out.println("replyRequestDto.getContent() = " + replyRequestDto.getContent());
        System.out.println("foundReply.getContent() = " + foundReply.getContent());
        System.out.println("foundReply.getReport().getStatus() = " + foundReply.getReport().getStatus());
        Assertions.assertEquals(replyRequestDto.getContent(), foundReply.getContent());
        Assertions.assertEquals(ReportStatus.Processed, foundReply.getReport().getStatus());
    }

    // 답변 수정 테스트
    @Test
    public void testUpdateReportReply() {
        //given
        User reporter = testUtils.createAndGetTestUser();
        User replier = testUtils.createAndGetTestUser();

        ReportRequestDto reportRequestDto = createReportRequest("버그가 있어요.", new ArrayList<>());
        long reportId = reportService.createReport(reporter, reportRequestDto);

        ReplyRequestDto replyRequestDto = createReplyRequest("확인해볼게요.");
        long replyId = reportReplyService.createReportReply(reportId, replyRequestDto, replier);

        //when
        ReplyRequestDto updateReply = createReplyRequest("이상 없어요.");
        reportReplyService.updateReportReply(replyId, updateReply);
        ReportReply foundReply = reportReplyService.getReportReply(replyId);

        //then
        System.out.println("updateReply.getContent() = " + updateReply.getContent());
        System.out.println("foundReply.getContent() = " + foundReply.getContent());
        Assertions.assertEquals(updateReply.getContent(), foundReply.getContent());
    }

    // 답변 삭제 테스트
    @Test
    public void testDeleteReportReply() {
        //given
        User reporter = testUtils.createAndGetTestUser();
        User replier = testUtils.createAndGetTestUser();

        ReportRequestDto reportRequestDto = createReportRequest("버그가 있어요.", new ArrayList<>());
        long reportId = reportService.createReport(reporter, reportRequestDto);

        ReplyRequestDto replyRequestDto = createReplyRequest("확인해볼게요.");
        long replyId = reportReplyService.createReportReply(reportId, replyRequestDto, replier);

        //when
        reportReplyService.deleteReportReply(replyId);

        //then
        Assertions.assertThrows(RuntimeException.class, () -> reportReplyService.getReportReply(replyId));
        Assertions.assertEquals(ReportStatus.Registered, reportService.getReportDetail(reportId).getReportStatus());
    }
}
