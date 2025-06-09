package com.kslj.mannam.domain.report.service;

import com.kslj.mannam.domain.notification.enums.NotificationType;
import com.kslj.mannam.domain.notification.service.NotificationService;
import com.kslj.mannam.domain.report.dto.ReplyRequestDto;
import com.kslj.mannam.domain.report.entity.Report;
import com.kslj.mannam.domain.report.entity.ReportReply;
import com.kslj.mannam.domain.report.enums.ReportStatus;
import com.kslj.mannam.domain.report.repository.ReportReplyRepository;
import com.kslj.mannam.domain.report.repository.ReportRepository;
import com.kslj.mannam.domain.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ReportReplyService {

    private final ReportRepository reportRepository;
    private final ReportReplyRepository replyRepository;
    private final NotificationService notificationService;

    // 답변 저장
    @Transactional
    public long createReportReply(long reportId, ReplyRequestDto replyRequestDto, User user) {

        // 넘겨온 reportId를 바탕으로 Report 조회
        Optional<Report> report = reportRepository.findById(reportId);
        if(report.isEmpty()) {
            throw new EntityNotFoundException("신고 내역을 찾을 수 없습니다. reportId = " + reportId);
        }

        // ReportReply 객체 생성
        ReportReply reply = ReportReply.builder()
                .content(replyRequestDto.getContent())
                .user(user)
                .report(report.get())
                .build();

        // 상태 업데이트(등록됨 -> 처리됨)
        report.get().updateStatus(ReportStatus.Processed);

        ReportReply savedReply = replyRepository.save(reply);
        notificationService.createNotification(NotificationType.Report, report.get().getReporter(), null);

        return savedReply.getId();
    }

    // 답변 조회
    @Transactional(readOnly = true)
    public ReportReply getReportReply(long reportId) {
        Optional<ReportReply> reportReply = replyRepository.findById(reportId);

        if (reportReply.isEmpty()) {
            throw new EntityNotFoundException("답변 내역을 찾을 수 없습니다. reportId=" + reportId);
        }

        return reportReply.get();
    }

    // 답변 수정
    @Transactional
    public long updateReportReply(long reportReplyId, ReplyRequestDto replyRequestDto) {
        Optional<ReportReply> reportReplyOpt = replyRepository.findById(reportReplyId);

        if (reportReplyOpt.isEmpty()) {
            throw new EntityNotFoundException("답변 내역을 찾을 수 없습니다. reportReplyId=" + reportReplyId);
        }

        ReportReply reportReply = reportReplyOpt.get();
        reportReply.updateContent(replyRequestDto.getContent());
        replyRepository.save(reportReply);

        return reportReply.getId();
    }

    // 답변 삭제
    @Transactional
    public long deleteReportReply(long replyId) {
        ReportReply reportReply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("답변 내역을 찾을 수 없습니다. replyId=" + replyId));

        Report report = reportReply.getReport();

        reportReply.unlinkReport();

        report.updateStatus(ReportStatus.Registered);

        replyRepository.delete(reportReply);

        return replyId;
    }
}
